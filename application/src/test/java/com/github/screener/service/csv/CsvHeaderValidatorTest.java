package com.github.screener.service.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CsvHeaderValidatorTest {

    private CsvHeaderValidator validator;
    private static final String[] EXPECTED_HEADERS = {
            "Column1", "Column2", "Column3", "Column4", "Column5"
    };

    @BeforeEach
    void setUp() {
        validator = new CsvHeaderValidator(EXPECTED_HEADERS);
    }

    @Test
    void shouldValidateCorrectHeaders() {
        String headerLine = "Column1,Column2,Column3,Column4,Column5";
        boolean result = validator.validateHeaders(headerLine, "test.csv");
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Column1,Column2",
            "WrongColumn1,Column2,Column3,Column4,Column5",
            "Column1,Column2,WrongColumn3,Column4,Column5",
            "Column1,Column2,Column3,Column4,WrongColumn5",
            "Column1,Column2,Column3,Column4,Column5,Extra1,Extra2"
    })
    void shouldRejectHeadersWithMismatch(String headerLine) {
        boolean result = validator.validateHeaders(headerLine, "test.csv");
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  Column1  ,  Column2  ,Column3,Column4,Column5",
            " Column1 , Column2 , Column3 , Column4 , Column5 ",
            "Column1 , Column2, Column3 ,Column4,Column5"
    })
    void shouldHandleHeadersWithWhitespace(String headerLine) {
        boolean result = validator.validateHeaders(headerLine, "test.csv");
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "column1,Column2,Column3,Column4,Column5",
            "Column1,column2,Column3,Column4,Column5",
            "COLUMN1,COLUMN2,COLUMN3,COLUMN4,COLUMN5"
    })
    void shouldRejectHeadersWithWrongCase(String headerLine) {
        boolean result = validator.validateHeaders(headerLine, "wrong-case-test.csv");
        assertFalse(result);
    }

    @Test
    void shouldGetExpectedHeaders() {
        String[] headers = validator.getExpectedHeaders();
        assertArrayEquals(EXPECTED_HEADERS, headers);
    }

    @Test
    void shouldGetExpectedColumnCount() {
        int count = validator.getExpectedColumnCount();
        assertEquals(5, count);
    }

    @Test
    void shouldHandleEmptyHeaderLine() {
        String headerLine = "";
        boolean result = validator.validateHeaders(headerLine, "test.csv");
        assertFalse(result);
    }
}
