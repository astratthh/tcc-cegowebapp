package com.example.cego_webapp.dto;

import com.example.cego_webapp.models.Funcionario;
import com.example.cego_webapp.validations.CpfOrCnpj;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FuncionarioDTO {

    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "")
    @CpfOrCnpj(message = "CPF/CNPJ inválido")
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

    public FuncionarioDTO() {} // construtor vazio

    // NOVO CONSTRUTOR
    public FuncionarioDTO(Funcionario funcionario) {
        this.nome = funcionario.getNome();
        this.documento = funcionario.getDocumento();
        this.email = funcionario.getEmail();
        this.telefone = funcionario.getTelefone();
        this.endereco = funcionario.getEndereco();
        this.cargo = funcionario.getCargo();
        this.salario = funcionario.getSalario();
    }
}
