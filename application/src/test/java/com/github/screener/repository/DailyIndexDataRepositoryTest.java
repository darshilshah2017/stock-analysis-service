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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@PostgresTestContainer
@ActiveProfiles("test")
class DailyIndexDataRepositoryTest {
    private static final Integer INDEX_ID = 1;
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
        DailyIndexData niftyDataToday = buildIndexData(INDEX_ID, today, 3500.00, 3510.00, 3450.00,
                3490.00, 0.50, 20.50, 25.73);

        DailyIndexData niftyDataTomorrow = buildIndexData(INDEX_ID, today.plusDays(1), 3510.00, 3530.00,
                3500.00, 3520.00, 0.52, 20.52, 25.75);

        repository.saveAll(List.of(niftyDataToday, niftyDataTomorrow));

        List<DailyIndexData> dailyIndexDataList = repository.findAllById(List.of(
                new DailyIndexData.DailyIndexDataId(INDEX_ID, today),
                new DailyIndexData.DailyIndexDataId(INDEX_ID, today.plusDays(1))
        ));
        assertThat(dailyIndexDataList)
                .hasSize(2)
                .anySatisfy(d -> {
                    assertThat(d.getId().getIndexId()).isEqualTo(INDEX_ID);
                    assertThat(d.getId().getDate()).isBetween(today.minusDays(1), today.plusDays(2));
                });
    }

    @Test
    @Transactional
    void shouldThrowExceptionWhenInsertingDuplicateRecord() {
        LocalDate today = LocalDate.now();
        DailyIndexData niftyDataToday = buildIndexData(INDEX_ID, today, 3500.00, 3510.00, 3450.00,
                3490.00, 0.50, 20.50, 25.73);

        entityManager.persist(niftyDataToday);

        DailyIndexData anotherNiftyData = buildIndexData(INDEX_ID, today, 3510.00, 3530.00,
                3500.00, 3520.00, 0.52, 20.52, 25.75);

        Assertions.assertThrows(EntityExistsException.class, () -> entityManager.persist(anotherNiftyData));
    }

    @Test
    void shouldThrowExceptionWhenInsertingRecordWithNonExistentIndexId() {
        Integer nonExistentIndexId = -1;
        DailyIndexData invalidIndexData = buildIndexData(nonExistentIndexId, LocalDate.now(), 3500.00, 3510.00,
                3450.00, 3490.00, 0.50, 20.50, 25.73);

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(invalidIndexData));
    }

    private DailyIndexData buildIndexData(
            Integer indexId,
            LocalDate date,
            double openValue,
            double highValue,
            double lowValue,
            double closeValue,
            double changePercentage,
            double peRatio,
            double pbRatio) {
        return DailyIndexData.builder()
                .withId(new DailyIndexData.DailyIndexDataId(indexId, date))
                .withOpenValue(BigDecimal.valueOf(openValue))
                .withHighValue(BigDecimal.valueOf(highValue))
                .withLowValue(BigDecimal.valueOf(lowValue))
                .withCloseValue(BigDecimal.valueOf(closeValue))
                .withChangePercentage(changePercentage)
                .withPeRatio(BigDecimal.valueOf(peRatio))
                .withPbRatio(BigDecimal.valueOf(pbRatio))
                .build();
    }
}