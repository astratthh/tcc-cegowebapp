package com.example.cego_webapp.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_servico")
@Data
@NoArgsConstructor
public class ItemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_cobrado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCobrado; // Preço do serviço no momento da OS

    public BigDecimal getSubtotal() {
        if (precoCobrado == null || quantidade == null) {
            return BigDecimal.ZERO;
        }
        return precoCobrado.multiply(new BigDecimal(quantidade));
    }

    // Construtor para facilitar a criação
    public ItemServico(OrdemServico os, Servico s, Integer qtd) {
        this.ordemServico = os;
        this.servico = s;
        this.quantidade = qtd;
        this.precoCobrado = s.getPreco(); // Pega o preço atual do serviço
    }
}