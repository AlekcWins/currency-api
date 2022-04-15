package ru.ds.education.currency.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "curs_data")
@Data
@NoArgsConstructor
public class Curs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    @Column(name = "curs")
    private BigDecimal curs;

    @Column(name = "curs_date")
    private LocalDate date;

}
