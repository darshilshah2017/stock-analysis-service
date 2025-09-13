package com.github.screener.service.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Utility class for converting CSV string values to typed values.
 * Handles null/dash values by converting to zero-equivalents.
 */
public class CsvTypeConverter {

    private static final Logger logger = LoggerFactory.getLogger(CsvTypeConverter.class);

    private CsvTypeConverter() {
    }

    /**
     * Checks if a string value should be treated as null/empty.
     *
     * @param value the value to check
     * @return true if the value is null, empty, or a dash
     */
    public static boolean isNullOrEmptyOrDash(String value) {
        return value == null || value.trim().isEmpty() || "-".equals(value.trim());
    }

    /**
     * Converts CSV string to BigDecimal.
     * Returns BigDecimal.ZERO for null/empty/dash values.
     *
     * @param value the CSV value
     * @return BigDecimal or BigDecimal.ZERO if null/empty/dash
     */
    public static BigDecimal toBigDecimal(String value) {
        if (isNullOrEmptyOrDash(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException _) {
            logger.warn("Failed to parse BigDecimal from value: {}", value);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Converts CSV string to Double.
     * Returns 0.0 for null/empty/dash values.
     *
     * @param value the CSV value
     * @return Double or 0.0 if null/empty/dash
     */
    public static Double toDouble(String value) {
        if (isNullOrEmptyOrDash(value)) {
            return 0.0;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException _) {
            logger.warn("Failed to parse Double from value: {}", value);
            return 0.0;
        }
    }

}
