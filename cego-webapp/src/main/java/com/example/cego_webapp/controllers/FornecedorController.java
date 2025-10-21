// src/main/java/com/example/cego_webapp/controllers/FornecedorController.java

package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.FornecedorDTO;
import com.example.cego_webapp.models.Fornecedor;
import com.example.cego_webapp.services.FornecedorService;
import com.example.cego_webapp.services.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @Autowired
    private PdfService pdfService;

    // ... (getFornecedores, createFornecedorForm, createFornecedor continuam iguais) ...
    @GetMapping({"", "/"})
    public String getFornecedores(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id,desc") String sort, @RequestParam(required = false) String keyword) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));
        Page<Fornecedor> fornecedoresPage = fornecedorService.listarFornecedores(keyword, pageable);
        model.addAttribute("fornecedoresPage", fornecedoresPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", fornecedoresPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);
        return "fornecedores/index";
    }
    @GetMapping("/create")
    public String createFornecedorForm(Model model) {
        if (!model.containsAttribute("fornecedorDTO")) {
            model.addAttribute("fornecedorDTO", new FornecedorDTO());
        }
        return "fornecedores/create";
    }
    @PostMapping("/create")
    public String createFornecedor(@Valid @ModelAttribute("fornecedorDTO") FornecedorDTO fornecedorDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fornecedorDTO", bindingResult);
            redirectAttributes.addFlashAttribute("fornecedorDTO", fornecedorDTO);
            return "redirect:/fornecedores/create";
        }
        try {
            fornecedorService.criarFornecedor(fornecedorDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Fornecedor cadastrado com sucesso!");
            return "redirect:/fornecedores";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("fornecedorDTO", "documento", e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fornecedorDTO", bindingResult);
            redirectAttributes.addFlashAttribute("fornecedorDTO", fornecedorDTO);
            return "redirect:/fornecedores/create";
        }
    }


    // ### MÉTODO CORRIGIDO ###
    @GetMapping("/edit")
    public String editFornecedorForm(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Fornecedor fornecedor = fornecedorService.buscarPorId(id);
            if (!model.containsAttribute("fornecedorDTO")) {
                model.addAttribute("fornecedorDTO", new FornecedorDTO(fornecedor));
            }

            // CORREÇÃO 1: Enviar o objeto inteiro com o nome "fornecedor"
            model.addAttribute("fornecedor", fornecedor);

            // CORREÇÃO 2: Ajustar o nome da página ativa
            model.addAttribute("activePage", "fornecedores");

            return "fornecedores/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/fornecedores";
        }
    }

    @PostMapping("/edit")
    public String editFornecedor(@RequestParam Integer id, @Valid @ModelAttribute("fornecedorDTO") FornecedorDTO fornecedorDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fornecedorDTO", bindingResult);
            redirectAttributes.addFlashAttribute("fornecedorDTO", fornecedorDTO);
            return "redirect:/fornecedores/edit?id=" + id;
        }
        try {
            fornecedorService.atualizarFornecedor(id, fornecedorDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Fornecedor atualizado com sucesso!");
            return "redirect:/fornecedores";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("fornecedorDTO", "documento", e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fornecedorDTO", bindingResult);
            redirectAttributes.addFlashAttribute("fornecedorDTO", fornecedorDTO);
            return "redirect:/fornecedores/edit?id=" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/fornecedores";
        }
    }

    @GetMapping("/delete")
    public String deleteFornecedor(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            fornecedorService.deletarFornecedor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Fornecedor excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // "Tradução" do erro
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o fornecedor, pois ele está associado a uma ou mais Compras.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/fornecedores";
    }

    @GetMapping("/relatorio/pdf")
    public void gerarRelatorioPdf(@RequestParam(required = false) String keyword, HttpServletResponse response) throws IOException {
        // Busca a lista completa de fornecedores
        List<Fornecedor> fornecedores = fornecedorService.listarTodosParaRelatorio(keyword);

        // Prepara as variáveis para o template
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("fornecedores", fornecedores);
        variaveis.put("dataGeracao", LocalDateTime.now());
        variaveis.put("totalFornecedores", fornecedores.size());

        // Gera o PDF
        byte[] pdfBytes = pdfService.gerarPdfDeHtml("fornecedor-relatorio.html", variaveis);

        // Envia o PDF para o navegador
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_fornecedores.pdf");
        response.getOutputStream().write(pdfBytes);
    }

}