package ru.ds.education.currency.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ds.education.currency.core.dto.CursDto;
import ru.ds.education.currency.core.dto.mapper.CursMapper;
import ru.ds.education.currency.model.CurrencyType;
import ru.ds.education.currency.model.Curs;
import ru.ds.education.currency.repository.CurrencyTypesRepo;
import ru.ds.education.currency.repository.CursRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CursService {
    private final CursMapper cursMapper;
    private final CursRepo cursRepo;
    private final CurrencyTypesRepo currencyTypesRepo;


    public Optional<CursDto> get(long id) {
        Optional<Curs> foundCurs = cursRepo.findById(id);
        return foundCurs.map(curs -> cursMapper.map(curs, CursDto.class));
    }

    public List<CursDto> getAll() {
        return cursRepo.findAll()
                .stream().map(c -> cursMapper.map(c, CursDto.class))
                .collect(Collectors.toList());
    }

    public Optional<CursDto> create(CursDto curs) {
        Curs newCurs = cursMapper.map(curs, Curs.class);
        Optional<CurrencyType> findCurrencyType = currencyTypesRepo.findAllByCurrencyType(curs.getCurrencyType());
        if (findCurrencyType.isPresent()) {
            newCurs.setCurrencyType(findCurrencyType.get());
            newCurs = cursRepo.save(newCurs);
            curs = cursMapper.map(newCurs, CursDto.class);
            return Optional.of(curs);
        }
        return Optional.empty();
    }

    public Optional<CursDto> update(CursDto curs) {
        Curs currentCurs = cursMapper.map(curs, Curs.class);
        Optional<CurrencyType> findCurrencyType = currencyTypesRepo.findAllByCurrencyType(curs.getCurrencyType());
        if (findCurrencyType.isPresent()) {
            long cursId = currentCurs.getId();
            currentCurs.setCurrencyType(findCurrencyType.get());
            Curs findCurs = cursRepo.findById(cursId)
                    .orElseThrow(() -> new NoSuchElementException("not found curs with id " + cursId));
            currentCurs.setId(findCurs.getId());
            Curs newCurs = cursRepo.save(currentCurs);
            curs = cursMapper.map(newCurs, CursDto.class);
            return Optional.of(curs);
        }
        return Optional.empty();
    }

    public void delete(long idCurs) {
        Optional<Curs> foundCourse = cursRepo.findById(idCurs);
        foundCourse.ifPresent(cursRepo::delete);

    }

    public List<CursDto> findAllByCurrencyType(String currencyType) {
        return cursRepo.findAllByCurrencyType(currencyType)
                .stream()
                .map(c -> cursMapper.map(c, CursDto.class))
                .collect(Collectors.toList());

    }

    public List<CursDto> findAllByCurrencyTypeAndDate(String currencyType, LocalDate date) {
        return cursRepo.findAllByCurrencyTypeAndDate(currencyType, date)
                .stream()
                .map(c -> cursMapper.map(c, CursDto.class))
                .collect(Collectors.toList());

    }

    public List<CursDto> findAllByDate(LocalDate date) {
        return cursRepo.findAllByDate(date)
                .stream()
                .map(c -> cursMapper.map(c, CursDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCursByDate(LocalDate onDate) {
        cursRepo.deleteCursByDate(onDate);
    }
}
