package com.example.cego_webapp.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdemServicoDTO {
    private Integer clienteId;
    private Integer veiculoId;
    private String observacoes;
    private LocalDateTime previsaoEntrega;
    private List<Integer> servicoIds;
    private List<Integer> funcionarioIds;

    // Construtor vazio padrão (gerado pelo @Data ou pode ser adicionado manualmente)
    public OrdemServicoDTO() {
    }
}