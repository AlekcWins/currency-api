package ru.ds.education.currency.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ds.education.currency.spec.DateSpec;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursDto {

    @Schema(description = "Id курса", example = "1")
    private long id;

    @Schema(description = "Тип валюты", example = "USD")
    private String currencyType;

    @Schema(description = "Значение курса", example = "1.5")
    private BigDecimal cursValue;

    @JsonFormat(pattern = DateSpec.DATE_FORMAT)
    @Schema(pattern = DateSpec.DATE_FORMAT, description = "дата", example = "14.04.2022")
    private LocalDate date;
}
