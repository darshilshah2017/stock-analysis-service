package com.github.screener.service;

import com.github.screener.dto.IndexPerformance;
import com.github.screener.repository.DailyIndexDataRepository;
import com.github.screener.repository.IndexPriceRangeProjection;
import com.github.screener.repository.MarketIndexRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexServiceTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Mock
    private DailyIndexDataRepository dailyIndexDataRepository;

    @Mock
    private MarketIndexRepository marketIndexRepository;

    private IndexService service;
    private File testFolder;

    @BeforeEach
    void setUp(@TempDir File tempDir) {
        testFolder = tempDir;
        service = new IndexService("daily-report/index", dailyIndexDataRepository, marketIndexRepository);
        ReflectionTestUtils.setField(service, "folder", testFolder);
    }

    private record TestProjection(Integer indexId, String indexName, BigDecimal startClose,
                                  BigDecimal endClose) implements IndexPriceRangeProjection {
        @Override
        public Integer getIndexId() {
            return indexId;
        }

        @Override
        public String getIndexName() {
            return indexName;
        }

        @Override
        public BigDecimal getStartClose() {
            return startClose;
        }

        @Override
        public BigDecimal getEndClose() {
            return endClose;
        }
    }

    @Test
    void shouldImportAllCsvFilesFromFolder() throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                Nifty 50,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                """;
        createCsvFile("test1.csv", csvContent);
        createCsvFile("test2.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Nifty 50")).thenReturn(1);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, times(2)).saveAll(anyList());
    }

    @Test
    void shouldContinueProcessingOtherFilesWhenOneFileFailsWithADatabaseError() throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                Nifty 50,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                """;
        createCsvFile("a_fails.csv", csvContent);
        createCsvFile("b_succeeds.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Nifty 50")).thenReturn(1);
        // First file's saveAll blows up (e.g. a DB constraint violation); the second file's should still be attempted.
        doThrow(new DataIntegrityViolationException("boom"))
                .doReturn(List.of())
                .when(dailyIndexDataRepository).saveAll(anyList());

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, times(2)).saveAll(anyList());
    }

    @Test
    void shouldNotFailWhenNoFilesFound() {
        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.txt", "data.json", "file.xml"})
    void shouldSkipFilesWithInvalidExtension(String fileName) throws Exception {
        createCsvFile(fileName, "some content");

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldHandleEmptyCsvFile() throws Exception {
        createCsvFile("empty.csv", "");

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldSkipFilesWithInvalidHeaders() throws Exception {
        String csvContent = """
                Wrong Header,Invalid Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                Nifty 50,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                """;
        createCsvFile("invalid_headers.csv", csvContent);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Index Name,Index Date,Open Index Value",
            "Nifty 50,01-01-2026,26173.3",
            "Nifty 50"
    })
    void shouldSkipLinesWithMissingColumns(String csvLine) throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                """ + csvLine;
        createCsvFile("incomplete.csv", csvContent);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ",01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28",
            "-,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28",
            "   ,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28"
    })
    void shouldSkipLinesWithEmptyIndexName(String csvLine) throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                """ + csvLine;
        createCsvFile("empty_name.csv", csvContent);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldSkipLinesWithUnknownIndexName() throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                Unknown Index,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                """;
        createCsvFile("unknown_index.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Unknown Index")).thenReturn(null);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Nifty 50,2026-01-01,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28",
            "Nifty 50,01/01/2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28",
            "Nifty 50,invalid-date,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28"
    })
    void shouldSkipLinesWithInvalidDateFormat(String csvLine) throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                """ + csvLine;
        createCsvFile("invalid_date.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Nifty 50")).thenReturn(1);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Nifty 50,01-01-2026,abc,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28",
            "Nifty 50,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,not-a-number,425631910,23454.66,22.76,3.56,1.28"
    })
    void shouldSkipLinesWithMalformedNumericValue(String csvLine) throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                """ + csvLine;
        createCsvFile("malformed_number.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Nifty 50")).thenReturn(1);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldHandleNullAndDashValuesAsZero() throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                Nifty 50,01-01-2026,-,-,-,-,-,-,425631910,23454.66,-,-,1.28
                """;
        createCsvFile("null_values.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Nifty 50")).thenReturn(1);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, times(1)).saveAll(anyList());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, 10})
    void shouldProcessMultipleLinesInSingleFile(int lineCount) throws Exception {
        StringBuilder csvContent = new StringBuilder(
                "Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield"
        ).append(System.lineSeparator());
        for (int i = 1; i <= lineCount; i++) {
            csvContent.append(String.format("Nifty %d,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28", i))
                    .append(System.lineSeparator());
        }

        createCsvFile("multiple_lines.csv", csvContent.toString());

        for (int i = 1; i <= lineCount; i++) {
            when(marketIndexRepository.findIndexIdByIndexName("Nifty " + i)).thenReturn(i);
        }

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldContinueProcessingWhenOneLineFailsInAFile() throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                Nifty 50,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                Unknown Index,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                Nifty Next 50,01-01-2026,69523.15,69722.15,69276.8,69675.4,310.9,.45,216114798,7311.28,20.27,3.62,1.41
                """;
        createCsvFile("mixed_valid_invalid.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Nifty 50")).thenReturn(1);
        when(marketIndexRepository.findIndexIdByIndexName("Unknown Index")).thenReturn(null);
        when(marketIndexRepository.findIndexIdByIndexName("Nifty Next 50")).thenReturn(2);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldHandleNoValidRecordsInFile() throws Exception {
        String csvContent = """
                Index Name,Index Date,Open Index Value,High Index Value,Low Index Value,Closing Index Value,Points Change,Change(%),Volume,Turnover (Rs. Cr.),P/E,P/B,Div Yield
                Unknown Index,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                ,01-01-2026,26173.3,26197.55,26113.4,26146.55,16.95,.06,425631910,23454.66,22.76,3.56,1.28
                """;
        createCsvFile("no_valid_records.csv", csvContent);

        when(marketIndexRepository.findIndexIdByIndexName("Unknown Index")).thenReturn(null);

        service.importDailyIndexData();

        verify(dailyIndexDataRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldReturnIndexesOutperformingNifty50SortedDescendingByReturn() {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(3);

        when(dailyIndexDataRepository.findCloseValuesAsOf(startDate, endDate)).thenReturn(List.of(
                new TestProjection(1, "Nifty 50", new BigDecimal("100"), new BigDecimal("110")),   // +10%
                new TestProjection(2, "Nifty Bank", new BigDecimal("100"), new BigDecimal("130")), // +30%
                new TestProjection(3, "Nifty IT", new BigDecimal("100"), new BigDecimal("120")),   // +20%
                new TestProjection(4, "Nifty Auto", new BigDecimal("100"), new BigDecimal("105"))  // +5% (underperforms)
        ));

        List<IndexPerformance> result = service.findOutperformingIndexes("Nifty 50", 3);

        assertThat(result).extracting(IndexPerformance::indexName)
                .containsExactly("Nifty Bank", "Nifty IT", "Nifty 50");
        assertThat(result.get(0).returnPercentage()).isEqualTo(30.0);
        assertThat(result.get(1).returnPercentage()).isEqualTo(20.0);
        assertThat(result.get(2).returnPercentage()).isEqualTo(10.0);
    }

    @Test
    void shouldIncludeBenchmarkInResults() {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(1);

        when(dailyIndexDataRepository.findCloseValuesAsOf(startDate, endDate)).thenReturn(List.of(
                new TestProjection(1, "Nifty 50", new BigDecimal("100"), new BigDecimal("110"))
        ));

        List<IndexPerformance> result = service.findOutperformingIndexes("Nifty 50", 1);

        assertThat(result).extracting(IndexPerformance::indexName).containsExactly("Nifty 50");
        assertThat(result.getFirst().returnPercentage()).isEqualTo(10.0);
    }

    @Test
    void shouldExcludeIndexesMissingStartOrEndPriceData() {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(6);

        // Nifty Bank has no data far enough back (e.g. index launched recently), so the underlying
        // start/end join naturally excludes it - it never appears in what the repository returns.
        // Nifty 50 (the benchmark) is still included, since it has data for the full period.
        when(dailyIndexDataRepository.findCloseValuesAsOf(startDate, endDate)).thenReturn(List.of(
                new TestProjection(1, "Nifty 50", new BigDecimal("100"), new BigDecimal("110"))
        ));

        List<IndexPerformance> result = service.findOutperformingIndexes("Nifty 50", 6);

        assertThat(result).extracting(IndexPerformance::indexName).containsExactly("Nifty 50");
    }

    @Test
    void shouldThrowWhenNifty50DataMissingForPeriod() {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(2);

        when(dailyIndexDataRepository.findCloseValuesAsOf(startDate, endDate)).thenReturn(List.of(
                new TestProjection(2, "Nifty Bank", new BigDecimal("100"), new BigDecimal("130"))
        ));

        assertThatThrownBy(() -> service.findOutperformingIndexes("Nifty 50", 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldMatchBenchmarkNameCaseInsensitively() {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(3);

        when(dailyIndexDataRepository.findCloseValuesAsOf(startDate, endDate)).thenReturn(List.of(
                new TestProjection(1, "Nifty 50", new BigDecimal("100"), new BigDecimal("110")),   // +10%
                new TestProjection(2, "Nifty Bank", new BigDecimal("100"), new BigDecimal("130"))  // +30%
        ));

        List<IndexPerformance> result = service.findOutperformingIndexes("nifty 50", 3);

        assertThat(result).extracting(IndexPerformance::indexName).containsExactly("Nifty Bank", "Nifty 50");
    }

    @Test
    void shouldThrowWhenFolderPathDoesNotExistOnClasspath() {
        assertThatThrownBy(() -> new IndexService("no-such-folder/on-classpath",
                dailyIndexDataRepository, marketIndexRepository))
                .isInstanceOf(IllegalStateException.class);
    }

    private void createCsvFile(String fileName, String content) throws IOException {
        File file = new File(testFolder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
