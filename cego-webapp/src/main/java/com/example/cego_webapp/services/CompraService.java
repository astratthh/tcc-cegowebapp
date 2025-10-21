// src/main/java/com/example/cego_webapp/services/CompraService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.CompraDTO;
import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.CompraRepository;
import com.example.cego_webapp.repositories.FornecedorRepository;
import com.example.cego_webapp.repositories.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CompraService {

    @Autowired private CompraRepository compraRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private ProdutoRepository produtoRepository;

    public Page<Compra> listarCompras(Pageable pageable) {
        return compraRepository.findAll(pageable);
    }

    public Compra buscarPorId(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra com ID " + id + " não encontrada."));
    }

    @Transactional
    public Compra criarCompra(CompraDTO compraDTO) {
        Fornecedor fornecedor = fornecedorRepository.findById(compraDTO.getFornecedorId())
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor inválido."));

        Compra compra = new Compra();
        compra.setFornecedor(fornecedor);
        compra.setDataCompra(LocalDateTime.now());
        compra.setStatus(CompraStatus.FINALIZADA);
        BigDecimal totalCompra = BigDecimal.ZERO;

        for (var itemDTO : compraDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).orElseThrow();
            produto.setEstoque(produto.getEstoque() + itemDTO.getQuantidade()); // AUMENTA o estoque

            ItemCompra itemCompra = new ItemCompra(null, compra, produto, itemDTO.getQuantidade(), itemDTO.getCustoUnitario());
            compra.getItens().add(itemCompra);
            totalCompra = totalCompra.add(itemDTO.getCustoUnitario().multiply(new BigDecimal(itemDTO.getQuantidade())));
        }

        compra.setValorTotal(totalCompra);
        ContaPagar contaPagar = new ContaPagar();
        contaPagar.setCompra(compra);
        contaPagar.setValor(totalCompra);
        contaPagar.setDataVencimento(compraDTO.getDataVencimento());
        contaPagar.setStatus(ContaPagarStatus.A_PAGAR);
        compra.setContaPagar(contaPagar);

        return compraRepository.save(compra);
    }

    @Transactional
    public Compra atualizarCompra(Long id, CompraDTO compraDTO) {
        Compra compra = buscarPorId(id);
        if (compra.getContaPagar() == null || compra.getContaPagar().getStatus() != ContaPagarStatus.A_PAGAR) {
            throw new IllegalStateException("A compra não pode mais ser editada.");
        }

        // 1. Reverte (diminui) o estoque dos itens antigos
        for (ItemCompra itemAntigo : compra.getItens()) {
            Produto produto = itemAntigo.getProduto();
            produto.setEstoque(produto.getEstoque() - itemAntigo.getQuantidade());
        }
        compra.getItens().clear();
        compraRepository.flush();
        BigDecimal novoTotalCompra = BigDecimal.ZERO;

        // 2. Adiciona os novos itens e atualiza (aumenta) o estoque
        for (var itemDTO : compraDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).orElseThrow();
            produto.setEstoque(produto.getEstoque() + itemDTO.getQuantidade());
            ItemCompra novoItem = new ItemCompra(null, compra, produto, itemDTO.getQuantidade(), itemDTO.getCustoUnitario());
            compra.getItens().add(novoItem);
            novoTotalCompra = novoTotalCompra.add(novoItem.getCustoUnitario().multiply(new BigDecimal(novoItem.getQuantidade())));
        }

        Fornecedor fornecedor = fornecedorRepository.findById(compraDTO.getFornecedorId()).orElseThrow();
        compra.setFornecedor(fornecedor);
        compra.setValorTotal(novoTotalCompra);
        compra.getContaPagar().setValor(novoTotalCompra);
        compra.getContaPagar().setDataVencimento(compraDTO.getDataVencimento());

        return compraRepository.save(compra);
    }

    @Transactional
    public void cancelarCompra(Long id) {
        Compra compra = buscarPorId(id);
        if (compra.getContaPagar() != null && compra.getContaPagar().getStatus() == ContaPagarStatus.PAGA) {
            throw new IllegalStateException("Não é possível cancelar uma compra que já foi paga.");
        }

        for (ItemCompra item : compra.getItens()) {
            Produto produto = item.getProduto();
            if (produto.getEstoque() < item.getQuantidade()) {
                throw new IllegalStateException("Cancelamento bloqueado: o produto '" + produto.getNome() + "' não tem estoque para a devolução.");
            }
            produto.setEstoque(produto.getEstoque() - item.getQuantidade()); // DIMINUI o estoque
        }

        compra.setStatus(CompraStatus.CANCELADA);
        compra.setDataCancelamento(LocalDateTime.now());
        if (compra.getContaPagar() != null) {
            compra.getContaPagar().setStatus(ContaPagarStatus.CANCELADA);
        }
        compraRepository.save(compra);
    }
}