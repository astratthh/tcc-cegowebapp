package com.example.cego_webapp.dto;

import com.example.cego_webapp.validations.CpfOrCnpj;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioDTO {

    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "")
    @CpfOrCnpj(message = "CPF inválido")
    private String documento;

    @Email
    @NotEmpty(message = "Email é obrigatório")
    private String email;

    @NotEmpty(message = "Endereço é obrigatório")
    private String endereco;

    @NotEmpty(message = "Telefone é obrigatório")
    private String telefone;

    @NotEmpty(message = "Cargo é obrigatório")
    private String cargo;

    @NotNull(message = "Salário é obrigatório")
    @DecimalMin(value = "0.01", message = "Salário deve ser maior que zero")
    private BigDecimal salario;

}
