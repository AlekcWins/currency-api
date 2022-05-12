package ru.ds.education.currency.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.ds.education.currency.model.CursRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CursRequestRepo extends PagingAndSortingRepository<CursRequest, Long> {

    Optional<CursRequest> getCursRequestByCorrelationId(String correlationId);

    List<CursRequest> getCursRequestByCursDate(LocalDate date, Pageable pageable);

    boolean existsCursRequestByCursDate(LocalDate date);
}
