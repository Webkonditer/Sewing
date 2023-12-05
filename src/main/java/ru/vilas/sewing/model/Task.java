package ru.vilas.sewing.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;


@Entity
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    private Category category;
    private String equipment;
    private int timeInSeconds;
    private BigDecimal costPerPiece;
    private int normPerShift;
    @OneToMany(mappedBy = "task")
    private Set<OperationData> operations;
}
