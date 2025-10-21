package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.Compra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CompraDTO {
    @NotNull(message = "Fornecedor é obrigatório")
    private Integer fornecedorId;

    @NotNull(message = "Data de Vencimento é obrigatória")
    private LocalDate dataVencimento;

    @Valid
    @NotEmpty(message = "A compra deve conter pelo menos um item")
    private List<ItemCompraDTO> itens = new ArrayList<>();

    // Construtor para carregar dados de uma Compra existente para a tela de edição
    public CompraDTO(Compra compra) {
        this.fornecedorId = compra.getFornecedor().getId();
        if (compra.getContaPagar() != null) {
            this.dataVencimento = compra.getContaPagar().getDataVencimento();
        }
        this.itens = compra.getItens().stream().map(itemCompra -> {
            ItemCompraDTO itemDTO = new ItemCompraDTO();
            itemDTO.setProdutoId(itemCompra.getProduto().getId());
            itemDTO.setQuantidade(itemCompra.getQuantidade());
            itemDTO.setNomeProduto(itemCompra.getProduto().getNome());
            itemDTO.setCustoUnitario(itemCompra.getCustoUnitario());
            return itemDTO;
        }).collect(Collectors.toList());
    }
}