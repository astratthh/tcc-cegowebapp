package com.example.cego_webapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Cliente implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

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
