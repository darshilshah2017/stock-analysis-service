package com.github.screener.integration.testcontainers.postgres;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.lang.annotation.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresTestContainerContextCustomizerFactory implements ContextCustomizerFactory {
    private static final String POSTGRES_DEFAULT_IMAGE = "huntress/postgres-partman";

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        if (AnnotatedElementUtils.hasAnnotation(testClass, PostgresTestContainer.class)) {
            PostgresTestContainer container = AnnotatedElementUtils.findMergedAnnotation(testClass, PostgresTestContainer.class);
            if (container == null) {
                throw new IllegalStateException("Failed to extract PostgresTestContainer from test class: " + testClass.getName());
            }
            return new PostgresTestContainerContextCustomizer(container);
        }
        return null;
    }

    private static class PostgresTestContainerContextCustomizer implements ContextCustomizer {
        private static final Map<String, PostgreSQLContainer<?>> POSTGRE_SQL_CONTAINER_MAP = new HashMap<>();
        private final PostgresTestContainer postgresTestContainer;

        public PostgresTestContainerContextCustomizer(PostgresTestContainer postgresTestContainer) {
            this.postgresTestContainer = postgresTestContainer;
        }

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            if (POSTGRE_SQL_CONTAINER_MAP.get(postgresTestContainer.version()) == null) {
                startContainer();
            }

            var postgresContainer = POSTGRE_SQL_CONTAINER_MAP.get(postgresTestContainer.version());
            var properties = Map.<String, Object>of(
                    "spring.datasource.url", postgresContainer.getJdbcUrl(),
                    "spring.datasource.username", postgresContainer.getUsername(),
                    "spring.datasource.password", postgresContainer.getPassword(),
                    "spring.test.database.replace", "NONE"
            );
            var propertySource = new MapPropertySource("PostgresContainer Test Properties", properties);
            context.getEnvironment().getPropertySources().addFirst(propertySource);
        }

        private synchronized void startContainer() {
            POSTGRE_SQL_CONTAINER_MAP.computeIfAbsent(postgresTestContainer.version(), version -> {
                final DockerImageName postgresImageName = DockerImageName.parse(POSTGRES_DEFAULT_IMAGE)
                        .withTag(version).asCompatibleSubstituteFor("postgres");
                var postgresContainer = new PostgreSQLContainer<>(postgresImageName)
                        .waitingFor(new HostPortWaitStrategy())
                        .withReuse(true);
                postgresContainer.start();

                try (Connection connection = createConnection(postgresContainer);
                     Statement statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS stock_analysis");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return postgresContainer;
            });
        }

        private Connection createConnection(PostgreSQLContainer<?> postgreSQLContainer) throws SQLException {
            return DriverManager.getConnection(postgreSQLContainer.getJdbcUrl(),
                    postgreSQLContainer.getUsername(),
                    postgreSQLContainer.getPassword());
        }

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    public @interface PostgresTestContainer {
        String version() default "16.8";
    }
}
