package com.example.cego_webapp.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ordens_servico")
@Data
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOrdemServico status;

    @Column(columnDefinition = "TEXT")
    private String observacoes; // Observações do cliente/serviço

    @Column(columnDefinition = "TEXT")
    private String observacoesInternas; // Para o histórico simples que sugerimos

    @Column(nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "data_entrada", nullable = false, updatable = false)
    private LocalDateTime dataEntrada;

    @Column(name = "data_previsao_entrega")
    private LocalDateTime previsaoEntrega;

    @UpdateTimestamp
    @Column(name = "data_ultima_atualizacao")
    private LocalDateTime dataUltimaAtualizacao;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    // Relação com os itens de serviço (OS tem muitos itens)
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemServico> itens = new ArrayList<>();

    // Relação com os funcionários (Muitos funcionários podem trabalhar em muitas OS)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "os_funcionarios",
            joinColumns = @JoinColumn(name = "ordem_servico_id"),
            inverseJoinColumns = @JoinColumn(name = "funcionario_id"))
    private Set<Funcionario> funcionarios = new HashSet<>();

    // Método auxiliar para recalcular o total
    public void recalcularValorTotal() {
        this.valorTotal = this.itens.stream()
                .map(ItemServico::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}