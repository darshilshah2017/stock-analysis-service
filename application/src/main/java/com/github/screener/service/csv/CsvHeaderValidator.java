package com.github.screener.service.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic CSV header validator that validates headers match expected columns.
 */
public class CsvHeaderValidator {

    private static final Logger logger = LoggerFactory.getLogger(CsvHeaderValidator.class);

    private final String[] expectedHeaders;

    public CsvHeaderValidator(String[] expectedHeaders) {
        this.expectedHeaders = expectedHeaders;
    }

    /**
     * Validates CSV header row matches expected headers.
     *
     * @param headerLine CSV header line (comma-separated)
     * @param fileName   file name for logging
     * @return true if valid, false otherwise
     */
    public boolean validateHeaders(String headerLine, String fileName) {
        String[] headers = headerLine.split(",", -1);

        if (headers.length != expectedHeaders.length) {
            logger.error("CSV file has incorrect column count. Expected: {}, Got: {} in file: {}",
                    expectedHeaders.length, headers.length, fileName);
            return false;
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            String actual = headers[i].trim();
            String expected = expectedHeaders[i];

            if (!actual.equals(expected)) {
                logger.error("CSV header mismatch at column {}. Expected: '{}', Got: '{}' in file: {}",
                        i, expected, actual, fileName);
                return false;
            }
        }

        logger.debug("CSV headers validated successfully for file: {}", fileName);
        return true;
    }

    public String[] getExpectedHeaders() {
        return expectedHeaders;
    }

    public int getExpectedColumnCount() {
        return expectedHeaders.length;
    }
}
