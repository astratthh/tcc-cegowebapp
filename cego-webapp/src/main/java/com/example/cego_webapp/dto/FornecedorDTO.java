package com.example.cego_webapp.dto;

import com.example.cego_webapp.validations.CpfOrCnpj;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FornecedorDTO {

    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "")
    @CpfOrCnpj(message = "CPF/CNPJ inválido")
    private String documento;

    @NotEmpty(message = "Endereço é obrigatório")
    private String endereco;

    @NotEmpty(message = "Telefone é obrigatório")
    private String telefone;

    @NotEmpty(message = "Email é obrigatório")
    @Email
    private String email;
    
}
