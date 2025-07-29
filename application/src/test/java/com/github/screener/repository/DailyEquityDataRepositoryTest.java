package com.github.screener.repository;

import com.github.screener.entity.DailyEquityData;
import com.github.screener.integration.testcontainers.postgres.PostgresTestContainerContextCustomizerFactory.PostgresTestContainer;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@PostgresTestContainer
@ActiveProfiles("test")
class DailyEquityDataRepositoryTest {
    private static final String SYMBOL = "TCS";
    @Autowired
    private DailyEquityDataRepository repository;
    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldPersistAndRetrieveMultipleRecordsForGivenSymbolWithDifferentDates() {
        LocalDate today = LocalDate.now();
        DailyEquityData tcsDataToday = buildEquityData(today,
                3500.00, 3510.00, 3550.00, 3490.00, 3520.00,
                3525.00, 3515.00, 100000, 350000000,
                5000, 40000, 40.00);

        DailyEquityData tcsDataTomorrow = buildEquityData(today.plusDays(1),
                3525.00, 3510.00, 3555.00, 3510.00, 3547.50,
                3545.00, 3530.00, 153000, 398200000,
                4500, 60000, 60.00);

        repository.saveAll(List.of(tcsDataToday, tcsDataTomorrow));

        List<DailyEquityData> dailyEquityDataList = repository.findAllById(List.of(
                new DailyEquityData.DailyEquityDataId(SYMBOL, today),
                new DailyEquityData.DailyEquityDataId(SYMBOL, today.plusDays(1))
        ));
        assertThat(dailyEquityDataList).hasSize(2);
    }

    @Test
    @Transactional
    void shouldThrowExceptionWhenInsertingDuplicateRecord() {
        LocalDate date = LocalDate.now();
        DailyEquityData tcsData = buildEquityData(date,
                3500.00, 3510.00, 3550.00, 3490.00, 3520.00,
                3525.00, 3515.00, 100000, 350000000,
                5000, 40000, 40.00);
        entityManager.persist(tcsData);

        DailyEquityData anotherTcsData = buildEquityData(date,
                3540.00, 3520.00, 3560.00, 3480.00, 3510.00,
                3535.00, 3525.00, 150000, 360000000, 5500,
                46000, 45.00);

        Assertions.assertThrows(EntityExistsException.class, () -> entityManager.persist(anotherTcsData));
    }

    private DailyEquityData buildEquityData(
            LocalDate date,
            double previousClose,
            double openPrice,
            double highPrice,
            double lowPrice,
            double lastPrice,
            double closePrice,
            double averagePrice,
            long totalTradedQuantity,
            long turnover,
            long numberOfTrades,
            long deliveryQuantity,
            double deliveryPercentage) {
        return DailyEquityData.builder()
                .withId(new DailyEquityData.DailyEquityDataId(SYMBOL, date))
                .withPreviousClose(BigDecimal.valueOf(previousClose))
                .withOpenPrice(BigDecimal.valueOf(openPrice))
                .withHighPrice(BigDecimal.valueOf(highPrice))
                .withLowPrice(BigDecimal.valueOf(lowPrice))
                .withLastPrice(BigDecimal.valueOf(lastPrice))
                .withClosePrice(BigDecimal.valueOf(closePrice))
                .withAveragePrice(BigDecimal.valueOf(averagePrice))
                .withTotalTradedQuantity(totalTradedQuantity)
                .withTurnover(turnover)
                .withNumberOfTrades(numberOfTrades)
                .withDeliveryQuantity(deliveryQuantity)
                .withDeliveryPercentage(BigDecimal.valueOf(deliveryPercentage))
                .build();
    }

}