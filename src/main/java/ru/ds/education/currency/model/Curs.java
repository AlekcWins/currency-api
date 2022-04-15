package ru.ds.education.currency.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "curs_data")
@Getter
@Setter
@NoArgsConstructor
public class Curs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    @Column(name = "curs")
    private BigDecimal cursValue;

    @Column(name = "curs_date")
    private LocalDate date;

}
