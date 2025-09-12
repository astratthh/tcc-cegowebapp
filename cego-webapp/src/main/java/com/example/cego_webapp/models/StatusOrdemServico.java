package com.example.cego_webapp.models;


import lombok.Getter;

@Getter
public enum StatusOrdemServico {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusOrdemServico(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}