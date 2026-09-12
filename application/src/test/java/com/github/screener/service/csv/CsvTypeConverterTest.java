package com.github.screener.service.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CsvTypeConverterTest {

    // Parameterized Tests for toBigDecimal()
    @ParameterizedTest
    @CsvSource({
            "123.45, 123.45",
            "100, 100",
            "-50.25, -50.25",
            "0, 0",
            "999.999, 999.999"
    })
    void shouldConvertValidNumberStringToBigDecimal(String input, String expected) {
        BigDecimal result = CsvTypeConverter.toBigDecimal(input);
        assertEquals(new BigDecimal(expected), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-", "   "})
    void shouldConvertMissingValueTokenToBigDecimalZero(String input) {
        BigDecimal result = CsvTypeConverter.toBigDecimal(input);
        assertEquals(BigDecimal.ZERO, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "xyz"})
    void shouldThrowForMalformedBigDecimalValue(String input) {
        assertThrows(NumberFormatException.class, () -> CsvTypeConverter.toBigDecimal(input));
    }

    @Test
    void shouldConvertNullToBigDecimalZero() {
        BigDecimal result = CsvTypeConverter.toBigDecimal(null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"  123.45  ", "  100  ", " -50.25 "})
    void shouldTrimWhitespaceBeforeConversionToBigDecimal(String input) {
        BigDecimal result = CsvTypeConverter.toBigDecimal(input);
        assertNotEquals(BigDecimal.ZERO, result);
    }

    // Parameterized Tests for toDouble()
    @ParameterizedTest
    @CsvSource({
            "45.67, 45.67",
            "100, 100.0",
            "-50.25, -50.25",
            "0, 0.0",
            "999.999, 999.999"
    })
    void shouldConvertValidNumberStringToDouble(String input, double expected) {
        Double result = CsvTypeConverter.toDouble(input);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-", "   "})
    void shouldConvertMissingValueTokenToDoubleZero(String input) {
        Double result = CsvTypeConverter.toDouble(input);
        assertEquals(0.0, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"xyz", "abc123"})
    void shouldThrowForMalformedDoubleValue(String input) {
        assertThrows(NumberFormatException.class, () -> CsvTypeConverter.toDouble(input));
    }

    @Test
    void shouldConvertNullToDoubleZero() {
        Double result = CsvTypeConverter.toDouble(null);
        assertEquals(0.0, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"  45.67  ", "  100  ", " -50.25 "})
    void shouldTrimWhitespaceBeforeConversionToDouble(String input) {
        Double result = CsvTypeConverter.toDouble(input);
        assertNotEquals(0.0, result);
    }

    // Parameterized Tests for isNullOrEmptyOrDash()
    @ParameterizedTest
    @ValueSource(strings = {"", "-", "   "})
    void shouldReturnTrueForEmptyDashOrWhitespace(String input) {
        assertTrue(CsvTypeConverter.isNullOrEmptyOrDash(input));
    }

    @Test
    void shouldReturnTrueForNull() {
        assertTrue(CsvTypeConverter.isNullOrEmptyOrDash(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "123", "test value", "data"})
    void shouldReturnFalseForValidNonEmptyString(String input) {
        assertFalse(CsvTypeConverter.isNullOrEmptyOrDash(input));
    }
}
