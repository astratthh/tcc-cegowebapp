package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.VendaDTO;
import com.example.cego_webapp.models.*; // Importa todos os models, incluindo VendaStatus
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

        // ... sua lógica de validação que já está correta ...
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

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataHora(LocalDateTime.now());
        // NOVO: Define o status da nova venda como REALIZADA
        venda.setStatus(VendaStatus.REALIZADA);

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

        vendaRepository.save(venda);

        return "redirect:/vendas";
    }

    // MÉTODO NOVO: Para cancelar a venda e retornar os itens ao estoque
    @GetMapping("/cancelar")
    public String cancelarVenda(@RequestParam Long id) {
        vendaRepository.findById(id).ifPresent(venda -> {
            // Apenas permite o cancelamento se a venda ainda estiver "REALIZADA"
            if (venda.getStatus() == VendaStatus.REALIZADA) {
                venda.setStatus(VendaStatus.CANCELADA);

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