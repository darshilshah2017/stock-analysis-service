package com.github.screener.integration;

import com.github.screener.entity.DailyIndexData;
import com.github.screener.integration.testcontainers.postgres.PostgresTestContainerContextCustomizerFactory.PostgresTestContainer;
import com.github.screener.repository.DailyIndexDataRepository;
import com.github.screener.repository.MarketIndexRepository;
import com.github.screener.service.IndexService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of {@link IndexService}: reads real CSV files off disk, resolves index
 * names against a real {@link MarketIndexRepository}, and persists rows into a real Postgres instance.
 */
@SpringBootTest
@PostgresTestContainer
@ActiveProfiles("test")
class IndexImportITest {

    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String CSV_HEADER = "Index Name,Index Date,Open Index Value,High Index Value,"
            + "Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield";

    @Autowired
    private IndexService indexService;
    @Autowired
    private DailyIndexDataRepository dailyIndexDataRepository;
    @Autowired
    private MarketIndexRepository marketIndexRepository;
    // Spring Boot's own auto-configured executor (no custom async config needed) - polled below so tests
    // don't have to guess how long the background import takes.
    @Autowired
    private ThreadPoolTaskExecutor applicationTaskExecutor;

    @TempDir
    private File csvFolder;

    @BeforeEach
    void setUp() {
        dailyIndexDataRepository.deleteAll();
        ReflectionTestUtils.setField(indexService, "folder", csvFolder);
    }

    /**
     * importDailyIndexData() is {@code @Async}, so it returns before the import actually runs.
     * Wait for the executor to go idle rather than asserting immediately or sleeping a guess.
     */
    private void importAndAwaitCompletion() {
        indexService.importDailyIndexData();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> applicationTaskExecutor.getActiveCount() == 0
                        && applicationTaskExecutor.getThreadPoolExecutor().getQueue().isEmpty());
    }

    @Test
    void shouldReadCsvFileAndPersistRowsIntoPostgres() throws IOException {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        Integer nifty50Id = marketIndexRepository.findIndexIdByIndexName("Nifty 50");

        writeCsvFile("ind_close_all_today.csv", List.of(
                csvRow("Nifty 50", today, "25867.10", "25923.65", "25728.00", "25839.65", "-.47", "22.50", "3.51"),
                csvRow("Nifty 50", tomorrow, "25839.65", "25950.00", "25800.00", "25900.00", ".23", "22.60", "3.52")
        ));

        importAndAwaitCompletion();

        List<DailyIndexData> persisted = dailyIndexDataRepository.findAllById(List.of(
                new DailyIndexData.DailyIndexDataId(nifty50Id, today),
                new DailyIndexData.DailyIndexDataId(nifty50Id, tomorrow)
        ));

        assertThat(persisted).hasSize(2);

        DailyIndexData todayRow = persisted.stream()
                .filter(row -> row.getId().getDate().equals(today))
                .findFirst()
                .orElseThrow();
        // did_*_value/did_pe_ratio/did_pb_ratio are NUMERIC(14,2), so Postgres always returns scale-2
        // BigDecimals matching these literals' scale exactly - safe to compare via equals() here.
        assertThat(todayRow).extracting(
                        DailyIndexData::getOpenValue,
                        DailyIndexData::getHighValue,
                        DailyIndexData::getLowValue,
                        DailyIndexData::getCloseValue,
                        DailyIndexData::getChangePercentage,
                        DailyIndexData::getPeRatio,
                        DailyIndexData::getPbRatio)
                .containsExactly(
                        new BigDecimal("25867.10"),
                        new BigDecimal("25923.65"),
                        new BigDecimal("25728.00"),
                        new BigDecimal("25839.65"),
                        -0.47,
                        new BigDecimal("22.50"),
                        new BigDecimal("3.51"));
    }

    @Test
    void shouldNotPersistRowsForUnknownIndexName() throws IOException {
        writeCsvFile("ind_close_all_unknown.csv", List.of(
                csvRow("Not A Real Index", LocalDate.now(), "100.00", "110.00", "90.00", "105.00", "1.5", "10.00", "1.00")
        ));

        importAndAwaitCompletion();

        assertThat(dailyIndexDataRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRollBackAllRowsInFileWhenOneRowViolatesAConstraint() throws IOException {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        Integer nifty50Id = marketIndexRepository.findIndexIdByIndexName("Nifty 50");

        writeCsvFile("ind_close_all_conflict.csv", List.of(
                // did_open_value is NUMERIC(14,2); 15 integer digits overflows it and fails at insert time.
                csvRow("Nifty 50", today, "999999999999999.00", "25923.65", "25728.00", "25839.65", "-.47", "22.50", "3.51"),
                // Otherwise-valid row that would succeed on its own, but is saved in the same batch/transaction.
                csvRow("Nifty 50", tomorrow, "25839.65", "25950.00", "25800.00", "25900.00", ".23", "22.60", "3.52")
        ));

        // The bad row's failure is caught and logged per-file, not propagated, so other files can still be processed.
        importAndAwaitCompletion();

        Optional<DailyIndexData> tomorrowRow = dailyIndexDataRepository.findById(
                new DailyIndexData.DailyIndexDataId(nifty50Id, tomorrow));
        assertThat(tomorrowRow).isEmpty();
    }

    private void writeCsvFile(String fileName, List<String> rows) throws IOException {
        File file = new File(csvFolder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(CSV_HEADER);
            writer.write(System.lineSeparator());
            for (String row : rows) {
                writer.write(row);
                writer.write(System.lineSeparator());
            }
        }
    }

    private String csvRow(String indexName, LocalDate date, String open, String high, String low, String close,
                          String changePercentage, String peRatio, String pbRatio) {
        return String.join(",",
                indexName,
                date.format(CSV_DATE_FORMATTER),
                open, high, low, close,
                "0",                    // Points Change (unused by the service)
                changePercentage,
                "0",                    // Volume (unused by the service)
                "0",                    // Turnover (unused by the service)
                peRatio, pbRatio,
                "0"                     // Div Yield (unused by the service)
        );
    }
}
