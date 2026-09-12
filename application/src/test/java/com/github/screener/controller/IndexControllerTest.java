package com.github.screener.controller;

import com.github.screener.dto.IndexPerformance;
import com.github.screener.service.IndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IndexControllerTest {

    @Mock
    private IndexService indexService;

    @InjectMocks
    private IndexControllerImpl underTest;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCallServiceExactlyOnceWhenImportIsRequested() throws Exception {
        mockMvc.perform(post("/api/indexes/import"))
                .andExpect(status().isAccepted());

        verify(indexService).importDailyIndexData();
    }

    @Test
    void shouldReturnInternalServerErrorWhenImportServiceThrowsRuntimeException() throws Exception {
        doThrow(new RuntimeException("Import failed")).when(indexService).importDailyIndexData();

        mockMvc.perform(post("/api/indexes/import"))
                .andExpect(status().isInternalServerError());

        verify(indexService).importDailyIndexData();
    }

    @Test
    void shouldReturnBadRequestWhenImportServiceThrowsIllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException("Invalid argument")).when(indexService).importDailyIndexData();

        mockMvc.perform(post("/api/indexes/import"))
                .andExpect(status().isBadRequest());

        verify(indexService).importDailyIndexData();
    }

    @Test
    void shouldReturnInternalServerErrorWhenImportServiceThrowsIllegalStateException() throws Exception {
        doThrow(new IllegalStateException("Invalid state")).when(indexService).importDailyIndexData();

        mockMvc.perform(post("/api/indexes/import"))
                .andExpect(status().isInternalServerError());

        verify(indexService).importDailyIndexData();
    }

    @Test
    void shouldHandleNullPointerExceptionFromImportService() throws Exception {
        doThrow(new NullPointerException("Service failed")).when(indexService).importDailyIndexData();

        mockMvc.perform(post("/api/indexes/import"))
                .andExpect(status().isInternalServerError());

        verify(indexService).importDailyIndexData();
    }

    @Test
    void shouldCallImportServiceOncePerRequest() throws Exception {
        mockMvc.perform(post("/api/indexes/import"))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/indexes/import"))
                .andExpect(status().isAccepted());

        verify(indexService, times(2)).importDailyIndexData();
    }

    @Test
    void shouldReturnOutperformersSortedDescending() throws Exception {
        when(indexService.findOutperformingIndexes("Nifty 50", 3)).thenReturn(List.of(
                new IndexPerformance("Nifty Bank", 30.0),
                new IndexPerformance("Nifty IT", 20.0)
        ));

        mockMvc.perform(get("/api/indexes/outperformers").param("benchmark", "Nifty 50").param("months", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].indexName").value("Nifty Bank"))
                .andExpect(jsonPath("$[0].returnPercentage").value(30.0))
                .andExpect(jsonPath("$[1].indexName").value("Nifty IT"))
                .andExpect(jsonPath("$[1].returnPercentage").value(20.0));

        verify(indexService).findOutperformingIndexes("Nifty 50", 3);
    }

    @Test
    void shouldReturnBadRequestWhenMonthsIsInvalid() throws Exception {
        when(indexService.findOutperformingIndexes("Nifty 50", 0))
                .thenThrow(new IllegalArgumentException("months must be a positive integer"));

        mockMvc.perform(get("/api/indexes/outperformers").param("benchmark", "Nifty 50").param("months", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUseDefaultBenchmarkAndMonthsWhenParamsAreMissing() throws Exception {
        when(indexService.findOutperformingIndexes("Nifty 50", 1)).thenReturn(List.of());

        mockMvc.perform(get("/api/indexes/outperformers"))
                .andExpect(status().isOk());

        verify(indexService).findOutperformingIndexes("Nifty 50", 1);
    }

    @Test
    void shouldReturnInternalServerErrorWhenBenchmarkDataIsMissing() throws Exception {
        when(indexService.findOutperformingIndexes("Nifty 50", 6))
                .thenThrow(new IllegalStateException("No data available for benchmark index 'Nifty 50' over the requested period"));

        mockMvc.perform(get("/api/indexes/outperformers").param("benchmark", "Nifty 50").param("months", "6"))
                .andExpect(status().isInternalServerError());
    }
}
