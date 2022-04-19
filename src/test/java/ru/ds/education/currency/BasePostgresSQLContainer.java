package ru.ds.education.currency;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;


@SpringBootTest(classes = CurrencyApiApplication.class)
@ContextConfiguration(initializers = {BasePostgresSQLContainer.Initializer.class})
public abstract class BasePostgresSQLContainer {
    public static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName("corp-digital")
            .withUsername("corp")
            .withPassword("corp");


    protected String readFileFromResource(String s) {
        Resource resource = new ClassPathResource(s, getClass().getClassLoader());
        try {
            return new String(
                    Files.readAllBytes(resource.getFile().toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext context) {
            if (!postgresSQLContainer.isRunning())
                postgresSQLContainer.start();
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgresSQLContainer.getPassword()
            ).applyTo(context.getEnvironment());
        }
    }
}
