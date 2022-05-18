package ru.ds.education.currency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.ds.education.currency.controller.CursController;
import ru.ds.education.currency.repository.CursRepo;
import ru.ds.education.currency.service.CursService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CurrencyApiApplicationTests  extends BaseActiveMQContainer {

    @Autowired
    private CursController cursController;

    @Autowired
    private CursService cursService;

    @Autowired
    private CursRepo cursRepo;


    @Test
    void contextLoads() {
        assertThat(cursController).isNotNull();
        assertThat(cursService).isNotNull();
        assertThat(cursRepo).isNotNull();
    }

}