package ru.ds.education.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CurrencyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyApiApplication.class, args);
    }

}
