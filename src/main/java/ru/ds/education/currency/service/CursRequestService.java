package ru.ds.education.currency.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.ds.education.currency.model.CursRequest;
import ru.ds.education.currency.model.CursRequestStatus;
import ru.ds.education.currency.repository.CursRequestRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class CursRequestService {
    private final CursRequestRepo cursRequestRepo;

    public Optional<CursRequest> create(LocalDate dateCurs) {
        if (!existsCursRequest(dateCurs)) {
            CursRequest cursRequest = CursRequest.builder()
                    .requestDate(LocalDateTime.now())
                    .status(CursRequestStatus.CREATED)
                    .cursDate(dateCurs)
                    .correlationId(UUID.randomUUID().toString())
                    .build();
            return Optional.of(cursRequestRepo.save(cursRequest));
        }
        return Optional.empty();
    }

    public void updateStatus(LocalDate date, CursRequestStatus status) {
        Optional<CursRequest> requestCursByDateLast = getRequestCursByDateLast(date);
        if (requestCursByDateLast.isPresent()) {
            String correlationId = requestCursByDateLast.get().getCorrelationId();
            CursRequest findCurs = cursRequestRepo.getCursRequestByCorrelationId(correlationId)
                    .orElseThrow(() -> new NoSuchElementException("not found curs request with id " + correlationId));
            findCurs.setStatus(status);
            cursRequestRepo.save(findCurs);
        }
    }

    public boolean existsCursRequest(LocalDate date) {
        return cursRequestRepo.existsCursRequestByCursDate(date);
    }

    public boolean existsCursRequestAndStatusNotFailed(LocalDate date) {
        boolean exist = cursRequestRepo.existsCursRequestByCursDate(date);
        if (exist) {
            Optional<CursRequest> requestDate = getRequestCursByDateLast(date);
            if (requestDate.isPresent()) {
                return !requestDate.get().getStatus().equals(CursRequestStatus.FAILED);
            }
        }
        return false;
    }

    private Optional<CursRequest> getRequestCursByDateLast(LocalDate date) {
        return Optional.ofNullable(cursRequestRepo.getCursRequestByCursDate(
                        date,
                        PageRequest.of(0, 1, Sort.Direction.DESC, "requestDate"))
                .get(0)
        );
    }
}
