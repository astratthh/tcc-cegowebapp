// src/main/java/com/example/cego_webapp/controllers/CompraController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.CompraDTO;
import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.ContaPagarRepository;
import com.example.cego_webapp.repositories.FornecedorRepository;
import com.example.cego_webapp.repositories.ProdutoRepository;
import com.example.cego_webapp.services.CompraService; // NOVO IMPORT
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
import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/compras")
public class CompraController {

    @Autowired private CompraService compraService;
    @Autowired private ContaPagarRepository contaPagarRepository; // Para o dashboard
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private ProdutoRepository produtoRepository;

    @GetMapping({"", "/"})
    public String index(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCompra"));
        Page<Compra> comprasPage = compraService.listarCompras(pageable);
        model.addAttribute("comprasPage", comprasPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comprasPage.getTotalPages());
        model.addAttribute("totalAPagar", contaPagarRepository.findTotalAPagar().orElse(BigDecimal.ZERO));
        model.addAttribute("totalVencido", contaPagarRepository.findTotalVencido().orElse(BigDecimal.ZERO));
        model.addAttribute("totalPagoMes", contaPagarRepository.findTotalPagoNoMes(LocalDate.now().withDayOfMonth(1)).orElse(BigDecimal.ZERO));
        model.addAttribute("activePage", "compras");
        return "compras/index";
    }

    @GetMapping("/create")
    public String createCompraForm(Model model) {
        if (!model.containsAttribute("compraDTO")) {
            model.addAttribute("compraDTO", new CompraDTO());
        }
        model.addAttribute("fornecedores", fornecedorRepository.findAll(Sort.by("nome")));
        model.addAttribute("produtos", produtoRepository.findAll(Sort.by("nome")));
        model.addAttribute("activePage", "compras");
        return "compras/create";
    }

    @PostMapping("/create")
    public String createCompra(@Valid @ModelAttribute("compraDTO") CompraDTO compraDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.compraDTO", bindingResult);
            redirectAttributes.addFlashAttribute("compraDTO", compraDTO);
            return "redirect:/compras/create";
        }
        try {
            Compra novaCompra = compraService.criarCompra(compraDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Compra #" + novaCompra.getId() + " registrada com sucesso!");
            return "redirect:/compras";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao registrar a compra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("compraDTO", compraDTO);
            return "redirect:/compras/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Compra compra = compraService.buscarPorId(id);
            if (compra.getContaPagar() == null || compra.getContaPagar().getStatus() != ContaPagarStatus.A_PAGAR) {
                redirectAttributes.addFlashAttribute("errorMessage", "Compras pagas ou canceladas não podem ser editadas.");
                return "redirect:/compras";
            }

            // Cria o DTO a partir da entidade para popular o formulário
            if (!model.containsAttribute("compraDTO")) {
                model.addAttribute("compraDTO", new CompraDTO(compra));
            }

            // Envia o objeto 'compra' para a tela (para usar o ID no título)
            model.addAttribute("compra", compra);

            // Envia as listas completas para os dropdowns e para o JavaScript
            model.addAttribute("fornecedores", fornecedorRepository.findAll(Sort.by("nome")));
            model.addAttribute("produtos", produtoRepository.findAll(Sort.by("nome")));

            return "compras/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/compras";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCompra(@PathVariable Long id, @Valid @ModelAttribute("compraDTO") CompraDTO compraDTO,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Verifique os campos obrigatórios.");
            redirectAttributes.addFlashAttribute("compraDTO", compraDTO);
            return "redirect:/compras/edit/" + id;
        }
        try {
            compraService.atualizarCompra(id, compraDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Compra #" + id + " atualizada com sucesso!");
            return "redirect:/compras";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar a compra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("compraDTO", compraDTO);
            return "redirect:/compras/edit/" + id;
        }
    }

    @GetMapping("/cancelar")
    public String cancelarCompra(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            compraService.cancelarCompra(id);
            redirectAttributes.addFlashAttribute("successMessage", "Compra #" + id + " cancelada e estoque ajustado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao cancelar a compra: " + e.getMessage());
        }
        return "redirect:/compras";
    }
}