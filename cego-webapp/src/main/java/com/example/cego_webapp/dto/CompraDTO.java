package com.example.cego_webapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CompraDTO {
    @NotNull
    private Integer fornecedorId;

    @NotNull
    private LocalDate dataVencimento; // Campo para o usuário digitar o vencimento

    @Valid @NotEmpty
    private List<ItemCompraDTO> itens = new ArrayList<>();
}