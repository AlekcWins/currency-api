package ru.ds.education.currency.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CursJMSDto implements Serializable {
    @JsonProperty("currency")
    private String currency;

    @JsonProperty("curs")
    private BigDecimal curs;
}
