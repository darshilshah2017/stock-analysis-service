package com.github.screener;

import com.github.screener.integration.testcontainers.postgres.PostgresTestContainerContextCustomizerFactory.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@PostgresTestContainer
@ActiveProfiles("test")
class StockAnalysisApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }
}