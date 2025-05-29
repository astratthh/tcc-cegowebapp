package com.example.cego_webapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Funcionario implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    @Column(name = "documento", nullable = false, unique = true)
    @NotEmpty(message = "CPF é obrigatório")
    private String documento;

    @Email
    @Column(nullable = false)
    @NotEmpty(message = "Email é obrigatório")
    private String email;

    @Column(nullable = false)
    @NotEmpty(message = "Email é obrigatório")
    private String endereco;

    @Column(nullable = false)
    @NotEmpty(message = "Telefone é obrigatório")
    private String telefone;

    @Column(nullable = false)
    @NotEmpty(message = "Cargo é obrigatório")
    private String cargo;

    @Column(nullable = false)
    @NotNull(message = "Salário é obrigatório")
    @DecimalMin(value = "0.01", message = "Salário deve ser maior que zero")
    private BigDecimal salario;
}
