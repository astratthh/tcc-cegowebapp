package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.Veiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDropdownDTO {
    private Integer id;
    private String placa;
    private String modelo;

    // Construtor que converte a Entidade para este DTO de resposta
    public VeiculoDropdownDTO(Veiculo veiculo) {
        this.id = veiculo.getId();
        this.placa = veiculo.getPlaca();
        this.modelo = veiculo.getModelo();
    }
}