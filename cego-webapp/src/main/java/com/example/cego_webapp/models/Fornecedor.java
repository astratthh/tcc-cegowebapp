package com.example.cego_webapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fornecedor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "CPF/CNPJ é obrigatório")
    private String documento;

    @Column(nullable = false)
    @NotEmpty(message = "Endereço é obrigatório")
    private String endereco;

    @Column(nullable = false)
    @NotEmpty(message = "Telefone é obrigatório")
    private String telefone;

    @Column(nullable = false)
    @NotEmpty(message = "Email é obrigatório")
    @Email
    private String email;
}
