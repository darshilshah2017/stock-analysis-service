package com.github.screener.service;

import com.github.screener.repository.DailyIndexDataRepository;
import com.github.screener.repository.MarketIndexRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyIndexDataServiceTest {

    @Mock
    private DailyIndexDataRepository dailyIndexDataRepository;

    @Mock
    private MarketIndexRepository marketIndexRepository;

    private DailyIndexDataService service;
    private File testFolder;

    @BeforeEach
    void setUp(@TempDir File tempDir) {
        testFolder = tempDir;
        service = new DailyIndexDataService("daily-report/index", dailyIndexDataRepository, marketIndexRepository);
        ReflectionTestUtils.setField(service, "folder", testFolder);
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

    // Helper methods
    private void createCsvFile(String fileName, String content) throws IOException {
        File file = new File(testFolder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
