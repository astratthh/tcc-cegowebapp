package com.example.cego_webapp.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "itens_venda")
@NoArgsConstructor // Lombok gera o construtor vazio
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private BigDecimal precoUnitario;

    // NOVO MÉTODO PARA CALCULAR O SUBTOTAL
    public BigDecimal getSubtotal() {
        if (precoUnitario == null) {
            return BigDecimal.ZERO;
        }
        return precoUnitario.multiply(new BigDecimal(quantidade));
    }

    // NOVO CONSTRUTOR PARA FACILITAR A CRIAÇÃO
    public ItemVenda(Venda venda, Produto produto, int quantidade) {
        this.venda = venda;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco(); // Pega o preço do produto no momento da venda
    }

    // Atalho para pegar o nome do produto diretamente
    public String getNomeProduto() {
        return this.produto != null ? this.produto.getNome() : "Produto não encontrado";
    }

    // Atalho para pegar o preço do produto diretamente
    public BigDecimal getPrecoProduto() {
        return this.produto != null ? this.produto.getPreco() : BigDecimal.ZERO;
    }
}