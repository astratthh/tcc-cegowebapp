package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.VendaDTO;
import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.ProdutoRepository;
import com.example.cego_webapp.repositories.VendaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaRepository vendaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping({"", "/"})
    public String getVendas(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataHora"));
        Page<Venda> vendasPage = vendaRepository.findAll(pageable);

        model.addAttribute("vendasPage", vendasPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", vendasPage.getTotalPages());

        return "vendas/index";
    }

    @GetMapping("/create")
    public String createVenda(Model model) {
        VendaDTO vendaDTO = new VendaDTO();
        model.addAttribute("vendaDTO", vendaDTO);
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("produtos", produtoRepository.findAll());
        return "vendas/create";
    }

    @PostMapping("/create")
    public String createVenda(@Valid @ModelAttribute VendaDTO vendaDTO, BindingResult bindingResult, Model model) {

        // ... lógica de validação (sem alterações) ...
        Cliente cliente = null;
        if (vendaDTO.getClienteId() != null) {
            cliente = clienteRepository.findById(vendaDTO.getClienteId()).orElse(null);
            if (cliente == null) {
                bindingResult.addError(new FieldError("vendaDTO", "clienteId", "Cliente selecionado é inválido."));
            }
        }
        if (vendaDTO.getItens() != null) {
            for (int i = 0; i < vendaDTO.getItens().size(); i++) {
                var itemDTO = vendaDTO.getItens().get(i);
                if (itemDTO == null || itemDTO.getProdutoId() == null) continue;
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).orElse(null);
                if (produto == null) {
                    bindingResult.addError(new FieldError("vendaDTO", "itens[" + i + "].produtoId", "Produto selecionado é inválido."));
                } else if (produto.getEstoque() < itemDTO.getQuantidade()) {
                    bindingResult.addError(new FieldError("vendaDTO", "itens[" + i + "].quantidade", "Estoque insuficiente para '" + produto.getNome() + "'. Disponível: " + produto.getEstoque()));
                }
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("clientes", clienteRepository.findAll());
            model.addAttribute("produtos", produtoRepository.findAll());
            return "vendas/create";
        }

        // --- LÓGICA DE NEGÓCIO ATUALIZADA ---
        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataHora(LocalDateTime.now());

        // MUDANÇA 1: O status inicial da venda agora é PENDENTE_PAGAMENTO
        venda.setStatus(VendaStatus.PENDENTE_PAGAMENTO);

        List<ItemVenda> itensVenda = new ArrayList<>();
        BigDecimal totalVenda = BigDecimal.ZERO;

        for (var itemDTO : vendaDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId()).get();
            ItemVenda itemVenda = new ItemVenda();
            itemVenda.setProduto(produto);
            itemVenda.setQuantidade(itemDTO.getQuantidade());
            itemVenda.setPrecoUnitario(produto.getPreco());
            itemVenda.setVenda(venda);
            itensVenda.add(itemVenda);
            BigDecimal subtotal = produto.getPreco().multiply(new BigDecimal(itemDTO.getQuantidade()));
            totalVenda = totalVenda.add(subtotal);
            produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
            produtoRepository.save(produto);
        }

        venda.setItens(itensVenda);
        venda.setValorTotal(totalVenda);

        // NOVO: Cria a Conta a Receber associada à venda
        ContaReceber contaReceber = new ContaReceber();
        contaReceber.setVenda(venda);
        contaReceber.setValor(venda.getValorTotal());
        contaReceber.setStatus(ContaReceberStatus.PENDENTE);
        // Define uma data de vencimento padrão de 30 dias
        contaReceber.setDataVencimento(LocalDate.now().plusDays(30));

        // Adiciona a conta à venda para o salvamento em cascata
        venda.setContaReceber(contaReceber);

        vendaRepository.save(venda); // Salva a venda, os itens e a conta a receber

        return "redirect:/vendas";
    }

    @GetMapping("/cancelar")
    public String cancelarVenda(@RequestParam Long id) {
        vendaRepository.findById(id).ifPresent(venda -> {
            // MUDANÇA 2: Apenas permite cancelar vendas com pagamento pendente
            if (venda.getStatus() == VendaStatus.PENDENTE_PAGAMENTO) {
                venda.setStatus(VendaStatus.CANCELADA);

                // NOVO: Cancela também a conta a receber associada
                ContaReceber conta = venda.getContaReceber();
                if (conta != null) {
                    conta.setStatus(ContaReceberStatus.CANCELADA);
                }

                // Devolve os itens da venda para o estoque
                for (ItemVenda item : venda.getItens()) {
                    Produto produto = item.getProduto();
                    if (produto != null) {
                        produto.setEstoque(produto.getEstoque() + item.getQuantidade());
                        produtoRepository.save(produto);
                    }
                }
                vendaRepository.save(venda); // Salva a venda com o novo status
            }
        });
        return "redirect:/vendas";
    }
}