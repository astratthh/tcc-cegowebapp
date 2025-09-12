package com.example.cego_webapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemCompraDTO {
    @NotNull
    private Integer produtoId;

    @NotNull @Min(1)
    private Integer quantidade;

    @NotNull @DecimalMin("0.01")
    private BigDecimal custoUnitario; // Campo para o usuário digitar o custo
}