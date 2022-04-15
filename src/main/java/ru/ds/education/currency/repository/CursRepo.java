package ru.ds.education.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ds.education.currency.model.CurrencyType;
import ru.ds.education.currency.model.Curs;

import java.time.LocalDate;
import java.util.List;

public interface CursRepo extends JpaRepository<Curs, Long> {

    List<Curs> findAllByCurrencyType(CurrencyType currencyType);

    List<Curs> findAllByDate(LocalDate date);

    List<Curs> findAllByCurrencyTypeAndDate(CurrencyType currencyType, LocalDate date);

}
