package ru.ds.education.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ds.education.currency.model.Curs;

import java.time.LocalDate;
import java.util.List;

public interface CursRepo extends JpaRepository<Curs, Long> {

    @Query(value = "SELECT c FROM  #{#entityName} c left JOIN c.currencyType as t" +
            " WHERE (:currencyType is null or t.currencyType = :currencyType)")
    List<Curs> findAllByCurrencyType(String currencyType);

    List<Curs> findAllByDate(LocalDate date);

    @Query(value = "SELECT c FROM  #{#entityName} c left JOIN c.currencyType as t" +
            " WHERE (:currency is null or t.currencyType = :currency) and" +
            "(cast(:date as date) is null or c.date = :date)")
    List<Curs> findAllByCurrencyTypeAndDate(@Param("currency") String currency,
                                            @Param("date") LocalDate date);

    void deleteCursByDate(LocalDate date);

}
