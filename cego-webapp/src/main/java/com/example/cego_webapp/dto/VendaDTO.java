package com.example.cego_webapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class VendaDTO {
    @NotNull(message = "O cliente é obrigatório")
    private Integer clienteId;

    @Valid // Valida os itens da lista
    @NotEmpty(message = "A venda deve ter pelo menos um item")
    private List<ItemVendaDTO> itens = new ArrayList<>();
}