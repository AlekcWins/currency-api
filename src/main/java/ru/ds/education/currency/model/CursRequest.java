package ru.ds.education.currency.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "curs_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "curs_date")
    private LocalDate cursDate;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CursRequestStatus status;
}
