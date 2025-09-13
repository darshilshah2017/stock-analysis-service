package com.github.screener.controller;

import com.github.screener.service.DailyIndexDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DailyIndexDataControllerTest {

    @Mock
    private DailyIndexDataService dailyIndexDataService;

    @InjectMocks
    private DailyIndexDataController underTest;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCallServiceExactlyOnceWhenImportIsRequested() throws Exception {
        mockMvc.perform(post("/api/daily-index-data/import"))
                .andExpect(status().isOk());

        verify(dailyIndexDataService).importDailyIndexData();
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceThrowsRuntimeException() throws Exception {
        doThrow(new RuntimeException("Import failed")).when(dailyIndexDataService).importDailyIndexData();

        mockMvc.perform(post("/api/daily-index-data/import"))
                .andExpect(status().isInternalServerError());

        verify(dailyIndexDataService).importDailyIndexData();
    }

    @Test
    void shouldReturnBadRequestWhenServiceThrowsIllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException("Invalid argument")).when(dailyIndexDataService).importDailyIndexData();

        mockMvc.perform(post("/api/daily-index-data/import"))
                .andExpect(status().isBadRequest());

        verify(dailyIndexDataService).importDailyIndexData();
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceThrowsIllegalStateException() throws Exception {
        doThrow(new IllegalStateException("Invalid state")).when(dailyIndexDataService).importDailyIndexData();

        mockMvc.perform(post("/api/daily-index-data/import"))
                .andExpect(status().isInternalServerError());

        verify(dailyIndexDataService).importDailyIndexData();
    }

    @Test
    void shouldHandleNullPointerExceptionFromService() throws Exception {
        doThrow(new NullPointerException("Service failed")).when(dailyIndexDataService).importDailyIndexData();

        mockMvc.perform(post("/api/daily-index-data/import"))
                .andExpect(status().isInternalServerError());

        verify(dailyIndexDataService).importDailyIndexData();
    }

    @Test
    void shouldCallServiceOncePerRequest() throws Exception {
        // First request
        mockMvc.perform(post("/api/daily-index-data/import"))
                .andExpect(status().isOk());

        // Second request
        mockMvc.perform(post("/api/daily-index-data/import"))
                .andExpect(status().isOk());

        // Verify service was called exactly twice (once per request)
        verify(dailyIndexDataService, times(2)).importDailyIndexData();
    }

}
