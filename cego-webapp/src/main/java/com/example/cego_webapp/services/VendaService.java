// src/main/java/com/example/cego_webapp/services/VendaService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.VendaDTO;
import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.ContaReceberRepository;
import com.example.cego_webapp.repositories.ProdutoRepository;
import com.example.cego_webapp.repositories.VendaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VendaService {

    @Autowired private VendaRepository vendaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ContaReceberRepository contaReceberRepository;

    public Page<Venda> listarVendas(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return vendaRepository.findByClienteNomeContainingIgnoreCase(keyword, pageable);
        } else {
            return vendaRepository.findAll(pageable);
        }
    }

    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venda com ID " + id + " não encontrada."));
    }

    @Transactional
    public Venda criarVenda(VendaDTO vendaDTO) {
        Cliente cliente = clienteRepository.findById(vendaDTO.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente selecionado é inválido."));

        if (vendaDTO.getItens() == null || vendaDTO.getItens().isEmpty()) {
            throw new IllegalArgumentException("A venda deve ter pelo menos um item.");
        }

        // Validação de estoque ANTES de qualquer alteração
        for (var itemDTO : vendaDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto inválido na lista."));
            if (produto.getEstoque() < itemDTO.getQuantidade()) {
                throw new IllegalStateException("Estoque insuficiente para '" + produto.getNome() + "'. Apenas " + produto.getEstoque() + " em estoque.");
            }
        }

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataHora(LocalDateTime.now());
        venda.setStatus(VendaStatus.PENDENTE_PAGAMENTO);
        BigDecimal totalVenda = BigDecimal.ZERO;

        for (var itemDTO : vendaDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).get();
            produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade()); // Baixa no estoque

            ItemVenda itemVenda = new ItemVenda(venda, produto, itemDTO.getQuantidade());
            venda.getItens().add(itemVenda);
            totalVenda = totalVenda.add(itemVenda.getSubtotal());
        }
        venda.setValorTotal(totalVenda);

        ContaReceber contaReceber = new ContaReceber();
        contaReceber.setVenda(venda);
        contaReceber.setValor(totalVenda);
        contaReceber.setStatus(ContaReceberStatus.PENDENTE);
        contaReceber.setDataVencimento(LocalDate.now().plusDays(30));
        venda.setContaReceber(contaReceber);

        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda atualizarVenda(Long id, VendaDTO vendaDTO) {
        Venda venda = buscarPorId(id);
        if (venda.getStatus() != VendaStatus.PENDENTE_PAGAMENTO) {
            throw new IllegalStateException("Apenas vendas com pagamento pendente podem ser editadas.");
        }

        Cliente cliente = clienteRepository.findById(vendaDTO.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente selecionado é inválido."));

        // 1. Estorna (devolve) o estoque dos itens antigos.
        for (ItemVenda itemAntigo : venda.getItens()) {
            Produto produto = itemAntigo.getProduto();
            produto.setEstoque(produto.getEstoque() + itemAntigo.getQuantidade());
        }
        venda.getItens().clear();
        vendaRepository.flush();

        // 2. Valida estoque e adiciona os novos itens, dando baixa.
        BigDecimal novoTotalVenda = BigDecimal.ZERO;
        for (var itemDTO : vendaDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).orElseThrow();
            if (produto.getEstoque() < itemDTO.getQuantidade()) {
                throw new IllegalStateException("Estoque insuficiente para '" + produto.getNome() + "'. A edição foi cancelada.");
            }
            produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
            ItemVenda novoItem = new ItemVenda(venda, produto, itemDTO.getQuantidade());
            venda.getItens().add(novoItem);
            novoTotalVenda = novoTotalVenda.add(novoItem.getSubtotal());
        }

        // Atualiza os dados da venda e da conta a receber
        venda.setCliente(cliente);
        venda.setValorTotal(novoTotalVenda);
        if (venda.getContaReceber() != null) {
            venda.getContaReceber().setValor(novoTotalVenda);
        }

        return vendaRepository.save(venda);
    }

    @Transactional
    public void cancelarVenda(Long id) {
        Venda venda = buscarPorId(id);
        if (venda.getStatus() != VendaStatus.PENDENTE_PAGAMENTO) {
            throw new IllegalStateException("Não é possível cancelar uma venda que já foi paga ou previamente cancelada.");
        }

        // Devolve os itens ao estoque
        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoque(produto.getEstoque() + item.getQuantidade());
        }

        venda.setStatus(VendaStatus.CANCELADA);
        venda.setDataCancelamento(LocalDateTime.now());
        if (venda.getContaReceber() != null) {
            venda.getContaReceber().setStatus(ContaReceberStatus.CANCELADA);
        }

        vendaRepository.save(venda);
    }
}