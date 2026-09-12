package com.github.screener.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler underTest = new GlobalExceptionHandler();

    @Test
    void shouldReturnInternalServerErrorForGenericException() {
        Exception exception = new Exception("Something went wrong");

        ResponseEntity<String> response = underTest.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An error occurred during import: Something went wrong");
    }

    @Test
    void shouldReturnInternalServerErrorForGenericExceptionWithNullMessage() {
        Exception exception = new Exception();

        ResponseEntity<String> response = underTest.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An error occurred during import: null");
    }

    @Test
    void shouldReturnInternalServerErrorForRuntimeException() {
        RuntimeException exception = new RuntimeException("Import failed");

        ResponseEntity<String> response = underTest.handleRuntimeException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Runtime error: Import failed");
    }

    @Test
    void shouldReturnBadRequestForIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<String> response = underTest.handleIllegalArgumentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid argument: Invalid argument");
    }

    @Test
    void shouldReturnInternalServerErrorForNullPointerException() {
        NullPointerException exception = new NullPointerException("Service failed");

        ResponseEntity<String> response = underTest.handleNullPointerException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Null pointer error: Service failed");
    }

    @Test
    void shouldReturnInternalServerErrorForNullPointerExceptionWithNullMessage() {
        NullPointerException exception = new NullPointerException();

        ResponseEntity<String> response = underTest.handleNullPointerException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Null pointer error: null");
    }
}
