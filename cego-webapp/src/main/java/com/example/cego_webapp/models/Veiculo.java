package com.example.cego_webapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Veiculo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "Placa é obrigatória")
    private String placa;

    @Column(nullable = false)
    @NotEmpty(message = "Marca é obrigatória")
    private String marca;

    @Column(nullable = false)
    @NotEmpty(message = "Modelo é obrigatório")
    private String modelo;

    @Column(nullable = false)
    @NotNull(message = "Ano é obrigatório")
    private Integer ano;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

}
