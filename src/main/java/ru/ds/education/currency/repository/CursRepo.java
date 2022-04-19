package ru.ds.education.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ds.education.currency.model.CurrencyType;
import ru.ds.education.currency.model.Curs;

import java.time.LocalDate;
import java.util.List;

public interface CursRepo extends JpaRepository<Curs, Long> {

    List<Curs> findAllByCurrencyType(CurrencyType currencyType);

    List<Curs> findAllByDate(LocalDate date);

    @Query(value = "SELECT c FROM  #{#entityName} c WHERE (:currency is null or c.currencyType = :currency) and" +
            "(cast(:date as date) is null or c.date = :date)")
    List<Curs> findAllByCurrencyTypeAndDate(@Param("currency") CurrencyType currency,
                                            @Param("date")  LocalDate date);

}
