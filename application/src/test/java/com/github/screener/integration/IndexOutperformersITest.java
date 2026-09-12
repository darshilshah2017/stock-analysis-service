package com.github.screener.integration;

import com.github.screener.entity.DailyIndexData;
import com.github.screener.integration.testcontainers.postgres.PostgresTestContainerContextCustomizerFactory.PostgresTestContainer;
import com.github.screener.repository.DailyIndexDataRepository;
import com.github.screener.repository.MarketIndexRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of {@code GET /api/indexes/outperformers} through the real Spring context (real
 * {@link MockMvc}, not a standalone one) so that the controller's {@code @Validated} constraints are
 * actually exercised - those only fire through a genuine Spring AOP proxy.
 */
@SpringBootTest
@AutoConfigureMockMvc
@PostgresTestContainer
@ActiveProfiles("test")
class IndexOutperformersITest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DailyIndexDataRepository dailyIndexDataRepository;
    @Autowired
    private MarketIndexRepository marketIndexRepository;

    @BeforeEach
    void setUp() {
        dailyIndexDataRepository.deleteAll();
    }

    @Test
    void shouldReturnOutperformersSortedDescending() throws Exception {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(3);
        Integer nifty50Id = marketIndexRepository.findIndexIdByIndexName("Nifty 50");
        Integer niftyBankId = marketIndexRepository.findIndexIdByIndexName("Nifty Bank");
        Integer niftyMetalId = marketIndexRepository.findIndexIdByIndexName("Nifty Metal");
        Integer niftyItId = marketIndexRepository.findIndexIdByIndexName("Nifty IT");
        Integer niftyAutoId = marketIndexRepository.findIndexIdByIndexName("Nifty Auto");

        seedClose(nifty50Id, startDate, "100.00");
        seedClose(nifty50Id, endDate, "110.00");     // +10% (benchmark)
        seedClose(niftyBankId, startDate, "100.00");
        seedClose(niftyBankId, endDate, "130.00");   // +30% (outperforms)
        seedClose(niftyMetalId, startDate, "100.00");
        seedClose(niftyMetalId, endDate, "120.00");  // +20% (outperforms)
        seedClose(niftyItId, startDate, "100.00");
        seedClose(niftyItId, endDate, "105.00");     // +5%  (underperforms - excluded)
        seedClose(niftyAutoId, startDate, "100.00");
        seedClose(niftyAutoId, endDate, "110.00");   // +10% (ties the benchmark - included, >= not strictly greater)

        mockMvc.perform(get("/api/indexes/outperformers")
                        .param("benchmark", "Nifty 50")
                        .param("months", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].indexName").value("Nifty Bank"))
                .andExpect(jsonPath("$[0].returnPercentage").value(30.0))
                .andExpect(jsonPath("$[1].indexName").value("Nifty Metal"))
                .andExpect(jsonPath("$[1].returnPercentage").value(20.0))
                // Nifty Auto (tied) and the benchmark itself both land last, both at 10% - the repository
                // query has no tiebreak ORDER BY, so their relative order between each other isn't
                // guaranteed; assert them as an unordered pair rather than fixed positions.
                .andExpect(jsonPath("$[2,3].indexName", containsInAnyOrder("Nifty Auto", "Nifty 50")))
                .andExpect(jsonPath("$[2,3].returnPercentage", everyItem(equalTo(10.0))));
    }

    @Test
    void shouldUseDefaultBenchmarkAndMonthsWhenParamsAreOmitted() throws Exception {
        LocalDate endDate = LocalDate.now(IST);
        LocalDate startDate = endDate.minusMonths(1);
        Integer nifty50Id = marketIndexRepository.findIndexIdByIndexName("Nifty 50");

        seedClose(nifty50Id, startDate, "100.00");
        seedClose(nifty50Id, endDate, "110.00");

        // No "benchmark"/"months" params -> defaults to Nifty 50 / 1 month.
        mockMvc.perform(get("/api/indexes/outperformers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnBadRequestWhenMonthsIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/indexes/outperformers")
                        .param("benchmark", "Nifty 50")
                        .param("months", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBenchmarkIsBlank() throws Exception {
        mockMvc.perform(get("/api/indexes/outperformers")
                        .param("benchmark", " ")
                        .param("months", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenBenchmarkHasNoData() throws Exception {
        // Table was cleared in setUp() and nothing is seeded, so there is no data for any index.
        mockMvc.perform(get("/api/indexes/outperformers")
                        .param("benchmark", "Nifty 50")
                        .param("months", "1"))
                .andExpect(status().isInternalServerError());
    }

    private void seedClose(Integer indexId, LocalDate date, String closeValue) {
        dailyIndexDataRepository.save(DailyIndexData.builder()
                .withId(new DailyIndexData.DailyIndexDataId(indexId, date))
                .withOpenValue(new BigDecimal(closeValue))
                .withHighValue(new BigDecimal(closeValue))
                .withLowValue(new BigDecimal(closeValue))
                .withCloseValue(new BigDecimal(closeValue))
                .withChangePercentage(0.0)
                .withPeRatio(BigDecimal.TEN)
                .withPbRatio(BigDecimal.ONE)
                .build());
    }
}
