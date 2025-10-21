package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.Servico;
import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ServicoDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    @Column(length = 255)
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", inclusive = true, message = "Preço deve ser maior que zero")
    @Column(nullable = false)
    private BigDecimal preco;

    public ServicoDTO() {} // construtor vazio

    // NOVO CONSTRUTOR
    public ServicoDTO(Servico servico) {
        this.setNome(servico.getNome());
        this.setDescricao(servico.getDescricao());
        this.setPreco(servico.getPreco());
    }
}
