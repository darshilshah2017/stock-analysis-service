package com.github.screener.service;

import com.github.screener.dto.DailyIndexDataRow;
import com.github.screener.dto.IndexPerformance;
import com.github.screener.entity.DailyIndexData;
import com.github.screener.repository.DailyIndexDataRepository;
import com.github.screener.repository.MarketIndexRepository;
import com.github.screener.service.csv.CsvHeaderValidator;
import com.github.screener.service.csv.CsvTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);
    private static final DateTimeFormatter INDEX_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static final String[] EXPECTED_HEADERS = {
            "Index Name",                   // 0
            "Index Date",                   // 1
            "Open Index Value",             // 2
            "High Index Value",             // 3
            "Low Index Value",              // 4
            "Closing Index Value",          // 5
            "Points Change",                // 6
            "Change(%)",                    // 7
            "Volume",                       // 8
            "Turnover (Rs. Cr.)",          // 9
            "P/E",                          // 10
            "P/B",                          // 11
            "Div Yield"                     // 12
    };

    private final DailyIndexDataRepository dailyIndexDataRepository;
    private final MarketIndexRepository marketIndexRepository;
    private final CsvHeaderValidator csvHeaderValidator;
    private final File folder;

    public IndexService(@Value("${stock-analysis.config.daily-index.folder-path}") String folderPath,
                         DailyIndexDataRepository dailyIndexDataRepository,
                         MarketIndexRepository marketIndexRepository) {
        this.dailyIndexDataRepository = dailyIndexDataRepository;
        this.marketIndexRepository = marketIndexRepository;
        this.csvHeaderValidator = new CsvHeaderValidator(EXPECTED_HEADERS);

        var dir = getClass().getClassLoader().getResource(folderPath);
        if (dir == null) {
            throw new IllegalStateException("Index folder not found at path: " + folderPath);
        }
        folder = new File(dir.getFile());
    }

    @Async
    public void importDailyIndexData() {
        File csvFolder = getFolder();
        File[] csvFiles = csvFolder.listFiles((_, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            logger.warn("No CSV files found in folder: {}", csvFolder.getAbsolutePath());
            return;
        }

        for (File csvFile : csvFiles) {
            try {
                importFile(csvFile);
            } catch (Exception e) {
                logger.error("Error importing file: {}", csvFile.getName(), e);
            }
        }
    }

    public List<IndexPerformance> findOutperformingIndexes(String benchmarkIndexName, int months) {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(months);

        List<IndexPerformance> returns = dailyIndexDataRepository.findCloseValuesAsOf(startDate, endDate).stream()
                .map(p -> new IndexPerformance(p.getIndexName(),
                        computeReturnPercentage(p.getStartClose(), p.getEndClose())))
                .toList();

        IndexPerformance benchmark = returns.stream()
                .filter(r -> benchmarkIndexName.equalsIgnoreCase(r.indexName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No data available for benchmark index '" + benchmarkIndexName + "' over the requested period"));

        return returns.stream()
                .filter(r -> r == benchmark || r.returnPercentage() >= benchmark.returnPercentage())
                .sorted(Comparator.comparingDouble(IndexPerformance::returnPercentage).reversed())
                .toList();
    }

    private void importFile(File csvFile) throws IOException {
        try (InputStream is = new FileInputStream(csvFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                logger.warn("CSV file is empty: {}", csvFile.getName());
                return;
            }

            if (!csvHeaderValidator.validateHeaders(headerLine, csvFile.getName())) {
                return;
            }

            // Parse all lines into DailyIndexDataRow objects
            List<DailyIndexDataRow> allRows = new ArrayList<>();
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    DailyIndexDataRow row = parseLineToRow(line, csvFile.getName());
                    if (row != null) {
                        allRows.add(row);
                    }
                } catch (Exception e) {
                    logger.debug("Error parsing line {} in file {}: {}", lineNumber, csvFile.getName(), e.getMessage());
                }
            }

            // Save all parsed rows in single transaction
            if (!allRows.isEmpty()) {
                saveAllRows(allRows, csvFile.getName());
                logger.info("Completed import from file: {} - Total lines: {}, Parsed and saved: {}",
                        csvFile.getName(), lineNumber - 1, allRows.size());
            } else {
                logger.warn("No valid records found in file: {}", csvFile.getName());
            }
        }
    }

    void saveAllRows(List<DailyIndexDataRow> rows, String fileName) {
        // Convert DailyIndexDataRow to DailyIndexData entities
        List<DailyIndexData> entities = new ArrayList<>(rows.size());
        for (DailyIndexDataRow row : rows) {
            DailyIndexData entity = DailyIndexData.builder()
                    .withId(new DailyIndexData.DailyIndexDataId(row.indexId(), row.date()))
                    .withOpenValue(row.openValue())
                    .withHighValue(row.highValue())
                    .withLowValue(row.lowValue())
                    .withCloseValue(row.closeValue())
                    .withChangePercentage(row.changePercentage())
                    .withPeRatio(row.peRatio())
                    .withPbRatio(row.pbRatio())
                    .build();
            entities.add(entity);
        }

        // Save all entities in single transaction
        // Hibernate will batch them using jdbc.batch_size=50 configured in application.yml
        dailyIndexDataRepository.saveAll(entities);
        logger.debug("Saved/Updated {} records from file: {}", entities.size(), fileName);
    }

    private DailyIndexDataRow parseLineToRow(String line, String fileName) {
        try {
            String[] tokens = line.split(",", -1);

            // Headers already validated, but data line might be malformed
            if (tokens.length < EXPECTED_HEADERS.length) {
                logger.debug("CSV line has insufficient columns (expected {}, got {}): {}",
                        EXPECTED_HEADERS.length, tokens.length, line);
                return null;
            }

            String indexName = tokens[0].trim();
            if (CsvTypeConverter.isNullOrEmptyOrDash(indexName)) {
                return null;
            }

            Integer indexId = marketIndexRepository.findIndexIdByIndexName(indexName);
            if (indexId == null) {
                logger.debug("Index ID not found for index name: {}", indexName);
                return null;
            }

            LocalDate indexDate = LocalDate.parse(tokens[1].trim(), INDEX_DATE_FORMATTER);

            return new DailyIndexDataRow(
                    indexId,
                    indexDate,
                    CsvTypeConverter.toBigDecimal(tokens[2]),      // openValue
                    CsvTypeConverter.toBigDecimal(tokens[3]),      // highValue
                    CsvTypeConverter.toBigDecimal(tokens[4]),      // lowValue
                    CsvTypeConverter.toBigDecimal(tokens[5]),      // closeValue
                    CsvTypeConverter.toDouble(tokens[7]),          // changePercentage
                    CsvTypeConverter.toBigDecimal(tokens[10]),     // peRatio
                    CsvTypeConverter.toBigDecimal(tokens[11])      // pbRatio
            );
        } catch (Exception e) {
            logger.debug("Error parsing line in file {}: {}", fileName, e.getMessage());
            return null;
        }
    }

    private double computeReturnPercentage(BigDecimal startClose, BigDecimal endClose) {
        return endClose.subtract(startClose)
                .divide(startClose, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    protected File getFolder() {
        return folder;
    }
}
