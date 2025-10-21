package com.example.cego_webapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ItemVendaDTO {

    @NotNull(message = "O produto de um item não pode ser nulo")
    private Integer produtoId;

    @NotNull(message = "A quantidade de um item não pode ser nula")
    @Min(value = 1, message = "A quantidade mínima para um item é 1")
    private int quantidade;

    private String nomeProduto;
    private BigDecimal precoUnitario;

}