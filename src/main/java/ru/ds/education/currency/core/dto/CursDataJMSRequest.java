package ru.ds.education.currency.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CursDataJMSRequest implements Serializable {
    @JsonProperty("onDate")
    private LocalDate onDate;
}
