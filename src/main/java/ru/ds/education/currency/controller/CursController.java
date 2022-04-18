package ru.ds.education.currency.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ds.education.currency.core.dto.CursDto;
import ru.ds.education.currency.model.CurrencyType;
import ru.ds.education.currency.service.CursService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ru.ds.education.currency.controller.CursController.ROUTE_PATH;

@RestController
@RequestMapping(value = ROUTE_PATH)
@Slf4j
public class CursController {

    public static final String ROUTE_PATH = "api/curs";
    private final CursService cursService;

    @Autowired
    public CursController(CursService cursService) {
        this.cursService = cursService;
    }

    @Operation(
            summary = "Получение курса по Id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "курс получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CursDto.class))}),
            @ApiResponse(responseCode = "400", description = "курс не найден",
                    content = @Content)})
    @GetMapping("/{cursId}")
    public ResponseEntity<CursDto> get(@PathVariable long cursId) {
        Optional<CursDto> foundCurs = cursService.get(cursId);
        return foundCurs
                .map(cursDto -> new ResponseEntity<>(cursDto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }


    @Operation(
            summary = "Получение курсов, необязательлные параметры date - дата ,currency -  тип валюты"
    )
    @GetMapping
    public List<CursDto> getAll(@RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date,
                                @RequestParam(required = false) CurrencyType currency) {

        return cursService.findAllByCurrencyTypeAndDate(currency, date);

    }

    @Operation(
            summary = "Добавление курса"
    )
    @PostMapping
    public CursDto create(@RequestBody CursDto newCurs) {
        return cursService.create(newCurs);
    }

    @Operation(
            summary = "Обновление курса"
    )
    @PutMapping
    public CursDto update(@RequestBody CursDto curs) {
        return cursService.update(curs);

    }

    @Operation(
            summary = "Удаление курса"
    )
    @DeleteMapping("/{cursId}")
    public void delete(@PathVariable long cursId) {
        cursService.delete(cursId);
    }

}
