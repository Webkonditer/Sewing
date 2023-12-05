package ru.vilas.sewing.dto;

import jakarta.persistence.*;
import lombok.Data;
import ru.vilas.sewing.model.OperationData;
import ru.vilas.sewing.model.Role;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class SeamstressDto {

    private Long seamstressId;
    private String seamstressName;
    private BigDecimal earnings;

}
