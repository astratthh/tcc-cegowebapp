package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.Veiculo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class VeiculoDTO {
    @NotEmpty(message = "Placa é obrigatória")
    private String placa;

    @NotEmpty(message = "Marca é obrigatória")
    private String marca;

    @NotEmpty(message = "Modelo é obrigatório")
    private String modelo;

    @NotNull(message = "Ano é obrigatório")
    private Integer ano;

    @NotNull(message = "Cliente é obrigatório")
    private Integer clienteId; // Para associar ao cliente

    public VeiculoDTO() {} // construtor vazio

    // NOVO CONSTRUTOR
    public VeiculoDTO(Veiculo veiculo) {
        this.setPlaca(veiculo.getPlaca());
        this.setMarca(veiculo.getMarca());
        this.setModelo(veiculo.getModelo());
        this.setAno(veiculo.getAno());
        this.setClienteId(veiculo.getCliente().getId());
    }
}
