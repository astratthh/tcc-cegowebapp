package com.example.cego_webapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data // Anotação do Lombok para gerar Getters, Setters, etc.
public class ItemVendaDTO {
    @NotNull(message = "O produto é obrigatório")
    private Integer produtoId;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade mínima é 1")
    private Integer quantidade;
}