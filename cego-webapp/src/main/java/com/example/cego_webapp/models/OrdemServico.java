package com.example.cego_webapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList; // Importe para inicializar a lista

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdemServico implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotNull(message = "Data de criação é obrigatória")
    private LocalDate dataCriacao;

    @Column
    private LocalDate dataConclusao;

    @ManyToOne
    @JoinColumn(name = "funcionario_id", nullable = false)
    @NotNull(message = "Funcionário é obrigatório")
    private Funcionario funcionario;

    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    @NotNull(message = "Veículo é obrigatório")
    private Veiculo veiculo;

    @ManyToMany // Relacionamento Many-to-Many com Servico
    @JoinTable(
            name = "ordem_servico_servico", // Nome da tabela de junção
            joinColumns = @JoinColumn(name = "ordem_servico_id"),
            inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    @NotNull(message = "É necessário selecionar ao menos um serviço")
    private List<Servico> servicos = new ArrayList<>(); // Inicializa a lista para evitar NullPointerException

    @Column(nullable = false)
    @NotBlank(message = "Status é obrigatório")
    private String status; // Ex: "Pendente", "Em Andamento", "Concluída", "Cancelada"

    @Column(length = 500) // Ajuste o tamanho conforme necessário
    private String observacoes;

    @Column(nullable = false)
    @NotNull(message = "O valor total é obrigatório")
    @DecimalMin(value = "0.00", inclusive = true, message = "O valor total não pode ser negativo")
    private BigDecimal valorTotal; // Campo para o valor total da ordem de serviço

}
