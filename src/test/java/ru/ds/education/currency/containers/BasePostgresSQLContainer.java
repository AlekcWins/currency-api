package ru.ds.education.currency.containers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;


@ContextConfiguration(initializers = {BasePostgresSQLContainer.Initializer.class})
@Slf4j
public abstract class BasePostgresSQLContainer {
    public static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName("corp-digital")
            .withUsername("corp")
            .withPassword("corp");



    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext context) {
            if (!postgresSQLContainer.isRunning()) {
                postgresSQLContainer.start();
                log.info("POSTGRES TESTCONTAINERS START URL" + postgresSQLContainer.getJdbcUrl());
            }
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgresSQLContainer.getPassword()
            ).applyTo(context.getEnvironment());
        }
    }
}
