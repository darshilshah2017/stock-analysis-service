package com.github.screener.repository;

import com.github.screener.entity.DailyIndexData;
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
class DailyIndexDataRepositoryTest {
    private static final String INDEX = "NIFTY 50";
    @Autowired
    private DailyIndexDataRepository repository;
    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldPersistAndRetrieveMultipleRecordsForGivenIndexWithDifferentDates() {
        LocalDate today = LocalDate.now();
        DailyIndexData niftyDataToday = buildIndexData(today, 3500.00, 3510.00, 3450.00,
                3490.00, 20.50, 25.73);

        DailyIndexData niftyDataTomorrow = buildIndexData(today.plusDays(1), 3510.00, 3530.00,
                3500.00, 3520.00, 20.52, 25.75);

        repository.saveAll(List.of(niftyDataToday, niftyDataTomorrow));

        List<DailyIndexData> dailyIndexDataList = repository.findAllById(List.of(
                new DailyIndexData.DailyIndexDataId(INDEX, today),
                new DailyIndexData.DailyIndexDataId(INDEX, today.plusDays(1))
        ));
        assertThat(dailyIndexDataList).hasSize(2);
    }

    @Test
    @Transactional
    void shouldThrowExceptionWhenInsertingDuplicateRecord() {
        LocalDate today = LocalDate.now();
        DailyIndexData niftyDataToday = buildIndexData(today, 3500.00, 3510.00, 3450.00,
                3490.00, 20.50, 25.73);

        entityManager.persist(niftyDataToday);

        DailyIndexData anotherNiftyData = buildIndexData(today, 3510.00, 3530.00,
                3500.00, 3520.00, 20.52, 25.75);

        Assertions.assertThrows(EntityExistsException.class, () -> entityManager.persist(anotherNiftyData));
    }

    private DailyIndexData buildIndexData(
            LocalDate date,
            double openValue,
            double highValue,
            double lowValue,
            double closeValue,
            double peRatio,
            double pbRatio) {
        return DailyIndexData.builder()
                .withId(new DailyIndexData.DailyIndexDataId(INDEX, date))
                .withOpenValue(BigDecimal.valueOf(openValue))
                .withHighValue(BigDecimal.valueOf(highValue))
                .withLowValue(BigDecimal.valueOf(lowValue))
                .withCloseValue(BigDecimal.valueOf(closeValue))
                .withPeRatio(BigDecimal.valueOf(peRatio))
                .withPbRatio(BigDecimal.valueOf(pbRatio))
                .build();
    }
}