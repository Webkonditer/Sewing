package ru.vilas.sewing.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.LocalDate;

@Entity
@Data
public class OperationData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Task task;

    @ManyToOne
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seamstress_id", nullable = false)
    private User seamstress;

    private LocalDate date;

    private int completedOperations;
}

