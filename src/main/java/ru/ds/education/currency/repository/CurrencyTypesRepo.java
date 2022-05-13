package ru.ds.education.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ds.education.currency.model.CurrencyType;

import java.util.Optional;

public interface CurrencyTypesRepo extends JpaRepository<CurrencyType, Long> {
    Optional<CurrencyType> findAllByCurrencyType(String currencyType);
}
