package com.example.cego_webapp.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "contas_a_receber")
public class ContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A conta pode vir de uma venda OU de uma OS, então ambos são opcionais
    @OneToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDate dataRecebimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContaReceberStatus status;

    @OneToOne
    @JoinColumn(name = "ordem_servico_id", unique = true)
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento") // Define o nome da coluna no banco
    private FormaPagamento formaPagamento;

    @Transient
    public String getClienteNome() {
        if (this.venda != null && this.venda.getCliente() != null) {
            return this.venda.getCliente().getNome();
        }
        if (this.ordemServico != null && this.ordemServico.getCliente() != null) {
            return this.ordemServico.getCliente().getNome();
        }
        return "N/A"; // Retorno padrão caso não haja cliente
    }

}
