// src/main/java/com/example/cego_webapp/dto/VendaDTO.java
package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.Venda;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class VendaDTO {
    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId;

    @Valid
    @NotEmpty(message = "A venda deve conter pelo menos um item")
    private List<ItemVendaDTO> itens = new ArrayList<>();

    // Construtor que carrega dados de uma Venda para o formulário de edição
    public VendaDTO(Venda venda) {
        this.clienteId = venda.getCliente().getId();
        this.itens = venda.getItens().stream().map(itemVenda -> {
            ItemVendaDTO itemDTO = new ItemVendaDTO();
            itemDTO.setProdutoId(itemVenda.getProduto().getId());
            itemDTO.setQuantidade(itemVenda.getQuantidade());
            itemDTO.setNomeProduto(itemVenda.getProduto().getNome());
            // O preço é pego do produto associado ao item da venda
            itemDTO.setPrecoUnitario(itemVenda.getProduto().getPreco());
            return itemDTO;
        }).collect(Collectors.toList());
    }
}