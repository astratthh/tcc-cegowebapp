package com.example.cego_webapp.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "vendas")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Muitas vendas para um cliente
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    // Uma venda tem muitos itens.
    // CascadeType.ALL: Salvar, atualizar ou deletar a Venda irá refletir nos Itens.
    // orphanRemoval = true: Se um ItemVenda for removido da lista, ele será deletado do banco.
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens;

    @Enumerated(EnumType.STRING) // Salva o nome do status ("REALIZADA", "CANCELADA") no banco
    @Column(nullable = false)
    private VendaStatus status;

    @OneToOne(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContaReceber contaReceber;

}