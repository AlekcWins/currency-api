package ru.ds.education.currency.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "currency_types")
@Getter
@Setter
@NoArgsConstructor
public class CurrencyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "currency")
    private String currencyType;
}
