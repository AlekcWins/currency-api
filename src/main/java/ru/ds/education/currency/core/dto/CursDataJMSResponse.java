package ru.ds.education.currency.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CursDataJMSResponse implements Serializable {
    @JsonProperty("onDate")
    private LocalDate onDate;

    @JsonProperty("rates")
    private List<CursJMSDto> rates;
}
