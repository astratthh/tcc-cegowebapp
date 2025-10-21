// src/main/java/com/example/cego_webapp/dto/ServicoDesempenhoDTO.java
package com.example.cego_webapp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServicoDesempenhoDTO {

    private Integer servicoId;
    private String nomeServico;
    private Long quantidadeRealizada;
    private BigDecimal faturamentoTotal;

    // Construtor que o Spring Data JPA usará para preencher os dados
    public ServicoDesempenhoDTO(Integer servicoId, String nomeServico, Long quantidadeRealizada, BigDecimal faturamentoTotal) {
        this.servicoId = servicoId;
        this.nomeServico = nomeServico;
        this.quantidadeRealizada = (quantidadeRealizada == null) ? 0L : quantidadeRealizada;
        this.faturamentoTotal = (faturamentoTotal == null) ? BigDecimal.ZERO : faturamentoTotal;
    }
}