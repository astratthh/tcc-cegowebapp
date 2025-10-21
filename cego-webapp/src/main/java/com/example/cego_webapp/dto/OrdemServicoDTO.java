// src/main/java/com/example/cego_webapp/dto/OrdemServicoDTO.java
package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.OrdemServico;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OrdemServicoDTO {
    private Integer clienteId;
    private Integer veiculoId;
    private String observacoes;
    private LocalDateTime previsaoEntrega;
    private List<Integer> servicoIds;
    private List<Integer> funcionarioIds;

    // Construtor para carregar dados de uma OS existente
    public OrdemServicoDTO(OrdemServico os) {
        this.clienteId = os.getCliente().getId();
        this.veiculoId = os.getVeiculo().getId();
        this.observacoes = os.getObservacoes();
        this.previsaoEntrega = os.getPrevisaoEntrega();
        this.servicoIds = os.getItens().stream()
                .map(item -> item.getServico().getId())
                .collect(Collectors.toList());
        this.funcionarioIds = os.getFuncionarios().stream()
                .map(f -> f.getId())
                .collect(Collectors.toList());
    }
}