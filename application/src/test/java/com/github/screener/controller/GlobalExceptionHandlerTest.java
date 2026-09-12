package com.github.screener.controller;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler underTest = new GlobalExceptionHandler();

    @Test
    void shouldReturnInternalServerErrorForGenericException() {
        Exception exception = new Exception("Something went wrong");

        ResponseEntity<String> response = underTest.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An error occurred during import");
    }

    @Test
    void shouldReturnInternalServerErrorForGenericExceptionWithNullMessage() {
        Exception exception = new Exception();

        ResponseEntity<String> response = underTest.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("An error occurred during import");
    }

    @Test
    void shouldReturnInternalServerErrorForRuntimeException() {
        RuntimeException exception = new RuntimeException("Import failed");

        ResponseEntity<String> response = underTest.handleRuntimeException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Runtime error occurred");
    }

    @Test
    void shouldReturnBadRequestForIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<String> response = underTest.handleIllegalArgumentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid argument");
    }

    @Test
    void shouldReturnInternalServerErrorForNullPointerException() {
        NullPointerException exception = new NullPointerException("Service failed");

        ResponseEntity<String> response = underTest.handleNullPointerException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Null pointer error");
    }

    @Test
    void shouldReturnInternalServerErrorForNullPointerExceptionWithNullMessage() {
        NullPointerException exception = new NullPointerException();

        ResponseEntity<String> response = underTest.handleNullPointerException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Null pointer error");
    }

    @Test
    void shouldReturnBadRequestForConstraintViolationException() {
        ConstraintViolationException exception = new ConstraintViolationException("months must be positive", Set.of());

        ResponseEntity<String> response = underTest.handleConstraintViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid request parameter");
    }

    @Test
    void shouldReturnBadRequestForMissingServletRequestParameterException() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("months", "int");

        ResponseEntity<String> response = underTest.handleMissingServletRequestParameterException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Missing required request parameter");
    }

    @Test
    void shouldReturnBadRequestForMethodArgumentTypeMismatchException() throws NoSuchMethodException {
        Method method = SampleTarget.class.getMethod("sample", int.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc", int.class, "months", methodParameter, new NumberFormatException("abc"));

        ResponseEntity<String> response = underTest.handleMethodArgumentTypeMismatchException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid request parameter");
    }

    private static class SampleTarget {
        public void sample(int months) {
            // Intentionally empty - never invoked, only reflected on to build a MethodParameter.
        }
    }
}
