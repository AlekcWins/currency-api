package ru.ds.education.currency.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ds.education.currency.core.dto.CursDto;
import ru.ds.education.currency.model.CursRequest;
import ru.ds.education.currency.service.CursRequestService;
import ru.ds.education.currency.service.CursService;
import ru.ds.education.currency.service.JMSCursRequestService;
import ru.ds.education.currency.spec.DateSpec;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.ds.education.currency.controller.CursController.ROUTE_PATH;

@RestController
@RequestMapping(value = ROUTE_PATH)
@Slf4j
@AllArgsConstructor
public class CursController {

    public static final String ROUTE_PATH = "api/curs";
    private final CursService cursService;
    private final CursRequestService cursRequestService;
    private final JMSCursRequestService jmsCursRequestService;


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
    @ApiResponse(responseCode = "200", description = "проверяется, есть запись в таблице curs_data. Если есть, то возвращается ответ",
            content = {@Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CursDto.class)))
            }
    )
    @ApiResponse(responseCode = "202", description = "Если нет записи в таблице curs_data," +
            " то проверяется есть ли запись в таблице curs_request. Выборка из таблицы осуществляется по curs_date, и по максимальной request_date." +
            " Если есть запись в таблице curs_request и статус ее не равен FAILED, то REST API  возвращает пустое тело;" +
            "Если нет записи в таблице curs_request или статус FAILED, то REST API  возвращает пустое тело, а в фоне" +
            " создается новая запись. Поле статус заполняется значением CREATED. проставляется correlationId рандомным uuid - ом" +
            " Далее  отправляется запрос в очередь запросов (dev.cbr.request)." +
            "Как только сообщение отправлено в очередь, статус у соответствующей записи  изменяется на SENT." +
            " Далее   слушается очередь dev.cbr.response. Вычитывается сообщение  по correlationId." +
            "удаляются все старые записе из таблицы cues_data по дате из ответа и создаются новые после помечается статус PROCESSED",
            content = @Content(schema = @Schema(hidden = true))
    )
    @GetMapping
    public ResponseEntity<List<CursDto>> getAll(@RequestParam(required = false) @DateTimeFormat(pattern = DateSpec.DATE_FORMAT) LocalDate date,
                                                @RequestParam(required = false) String currency) {
        List<CursDto> allByCurrencyTypeAndDate = cursService.findAllByCurrencyTypeAndDate(currency, date);
        if (allByCurrencyTypeAndDate.isEmpty() && !Objects.isNull(date)) {
            if (!cursRequestService.existsCursRequestAndStatusNotFailed(date)) {
                Optional<CursRequest> cursRequest = cursRequestService.create(date);
                if (cursRequest.isPresent()) {
                    CursRequest savedCursRequest = cursRequest.get();
                    jmsCursRequestService.sendAndReceive(savedCursRequest.getCursDate(), savedCursRequest.getCorrelationId());

                }
            }
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

        return new ResponseEntity<>(allByCurrencyTypeAndDate, HttpStatus.OK);

    }

    @Operation(
            summary = "Добавление курса"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "курс создан",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CursDto.class))}),
            @ApiResponse(responseCode = "400", description = "ошибка создания курса",
                    content = @Content)})
    @PostMapping
    public ResponseEntity<CursDto> create(@RequestBody CursDto newCurs) {
        Optional<CursDto> createCurs = cursService.create(newCurs);
        return createCurs
                .map(cursDto -> new ResponseEntity<>(cursDto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Operation(
            summary = "Обновление курса"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "курс обновлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CursDto.class))}),
            @ApiResponse(responseCode = "400", description = "ошибка обновления курса",
                    content = @Content)})
    @PutMapping
    public ResponseEntity<CursDto> update(@RequestBody CursDto curs) {
        return cursService.update(curs)
                .map(cursDto -> new ResponseEntity<>(cursDto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    }

    @Operation(
            summary = "Удаление курса"
    )
    @DeleteMapping("/{cursId}")
    public void delete(@PathVariable long cursId) {
        cursService.delete(cursId);
    }

}
