// src/main/java/com/example/cego_webapp/controllers/VendaController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.VendaDTO;
import com.example.cego_webapp.models.Venda;
import com.example.cego_webapp.models.VendaStatus;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.ProdutoRepository;
import com.example.cego_webapp.services.VendaService; // NOVO IMPORT
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired private VendaService vendaService;

    // Repositórios para popular dropdowns dos formulários
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ProdutoRepository produtoRepository;

    @GetMapping({"", "/"})
    public String getVendas(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataHora"));
        Page<Venda> vendasPage = vendaService.listarVendas(keyword, pageable);

        model.addAttribute("vendasPage", vendasPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", vendasPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        return "vendas/index";
    }

    @GetMapping("/create")
    public String createVendaForm(Model model) {
        if (!model.containsAttribute("vendaDTO")) {
            model.addAttribute("vendaDTO", new VendaDTO());
        }
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
        model.addAttribute("produtos", produtoRepository.findAll(Sort.by("nome")));
        return "vendas/create";
    }

    @PostMapping("/create")
    public String createVenda(@Valid @ModelAttribute("vendaDTO") VendaDTO vendaDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.vendaDTO", bindingResult);
            redirectAttributes.addFlashAttribute("vendaDTO", vendaDTO);
            return "redirect:/vendas/create";
        }
        try {
            Venda novaVenda = vendaService.criarVenda(vendaDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Venda #" + novaVenda.getId() + " registrada com sucesso!");
            return "redirect:/vendas";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("vendaDTO", vendaDTO);
            return "redirect:/vendas/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Venda venda = vendaService.buscarPorId(id);

            if (venda.getStatus() != VendaStatus.PENDENTE_PAGAMENTO) {
                redirectAttributes.addFlashAttribute("errorMessage", "Apenas vendas com pagamento pendente podem ser editadas.");
                return "redirect:/vendas";
            }

            if (!model.containsAttribute("vendaDTO")) {
                model.addAttribute("vendaDTO", new VendaDTO(venda));
            }

            model.addAttribute("venda", venda);
            model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));

            // ### CORREÇÃO APLICADA AQUI ###
            // Renomeie 'produtosJson' para 'produtos'.
            // A tela espera a lista de produtos para o dropdown e para a lógica JavaScript.
            model.addAttribute("produtos", produtoRepository.findAll(Sort.by("nome")));

            return "vendas/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vendas";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateVenda(@PathVariable Long id, @Valid @ModelAttribute("vendaDTO") VendaDTO vendaDTO,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.vendaDTO", bindingResult);
            redirectAttributes.addFlashAttribute("vendaDTO", vendaDTO);
            return "redirect:/vendas/edit/" + id;
        }
        try {
            vendaService.atualizarVenda(id, vendaDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Venda #" + id + " atualizada com sucesso!");
            return "redirect:/vendas";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("vendaDTO", vendaDTO);
            return "redirect:/vendas/edit/" + id;
        }
    }

    @GetMapping("/cancelar")
    public String cancelarVenda(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            vendaService.cancelarVenda(id);
            redirectAttributes.addFlashAttribute("successMessage", "Venda #" + id + " cancelada e estoque restaurado!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/vendas";
    }
}