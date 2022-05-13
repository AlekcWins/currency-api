package ru.ds.education.currency;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;



@ContextConfiguration(initializers = {BaseActiveMQContainer.Initializer.class})
@Slf4j
public abstract class BaseActiveMQContainer extends BasePostgresSQLContainer {

    public static GenericContainer activeMqContainer = new GenericContainer(DockerImageName.parse("webcenter/activemq:5.14.3"))
            .withStartupTimeout(Duration.ofSeconds(9))
            .withExposedPorts(61616, 8161);


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext context) {
            if (!activeMqContainer.isRunning())
                activeMqContainer.start();
            log.info("ActiveMQ container PORT : " + activeMqContainer.getFirstMappedPort());
            final String url = String.format("tcp://localhost:%d", activeMqContainer.getFirstMappedPort());
            TestPropertyValues.of(
                            "spring.activemq.broker-url=" + url)
                    .applyTo(context.getEnvironment());
        }
    }

}
