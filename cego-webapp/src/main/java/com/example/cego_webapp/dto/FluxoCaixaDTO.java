// src/main/java/com/example/cego_webapp/dto/FluxoCaixaDTO.java
package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.FormaPagamento;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FluxoCaixaDTO {
    private FormaPagamento formaPagamento;
    private BigDecimal total;

    // Construtor obrigatório para a query JPQL funcionar
    public FluxoCaixaDTO(FormaPagamento formaPagamento, BigDecimal total) {
        this.formaPagamento = formaPagamento;
        this.total = (total == null) ? BigDecimal.ZERO : total; // Garante que não seja nulo
    }

    // Método auxiliar para exibir a descrição no relatório
    public String getFormaPagamentoDescricao() {
        if (formaPagamento == null) {
            return "Não Identificado";
        }
        return formaPagamento.getDescricao();
    }
}