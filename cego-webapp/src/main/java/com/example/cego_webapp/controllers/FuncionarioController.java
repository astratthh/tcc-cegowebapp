// src/main/java/com/example/cego_webapp/controllers/FuncionarioController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.FuncionarioDTO;
import com.example.cego_webapp.models.Funcionario;
import com.example.cego_webapp.services.FuncionarioService; // NOVO IMPORT
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
@RequestMapping("/funcionarios")
public class FuncionarioController {

    @Autowired
    private FuncionarioService funcionarioService; // INJETA O SERVICE

    @Autowired
    private PdfService pdfService;

    @GetMapping({"", "/"})
    public String getFuncionarios(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "id,desc") String sort,
                                  @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        Page<Funcionario> funcionariosPage = funcionarioService.listarFuncionarios(keyword, pageable);

        model.addAttribute("funcionariosPage", funcionariosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", funcionariosPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "funcionarios");

        return "funcionarios/index";
    }

    @GetMapping("/create")
    public String createFuncionarioForm(Model model) {
        if (!model.containsAttribute("funcionarioDTO")) {
            model.addAttribute("funcionarioDTO", new FuncionarioDTO());
        }
        model.addAttribute("activePage", "funcionarios");
        return "funcionarios/create";
    }

    @PostMapping("/create")
    public String createFuncionario(@Valid @ModelAttribute("funcionarioDTO") FuncionarioDTO funcionarioDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.funcionarioDTO", bindingResult);
            redirectAttributes.addFlashAttribute("funcionarioDTO", funcionarioDTO);
            return "redirect:/funcionarios/create";
        }
        try {
            funcionarioService.criarFuncionario(funcionarioDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Funcionário cadastrado com sucesso!");
            return "redirect:/funcionarios";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("funcionarioDTO", "documento", e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.funcionarioDTO", bindingResult);
            redirectAttributes.addFlashAttribute("funcionarioDTO", funcionarioDTO);
            return "redirect:/funcionarios/create";
        }
    }

    @GetMapping("/edit")
    public String editFuncionarioForm(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Funcionario funcionario = funcionarioService.buscarPorId(id);
            if (!model.containsAttribute("funcionarioDTO")) {
                // Adicione este construtor ao seu FuncionarioDTO
                model.addAttribute("funcionarioDTO", new FuncionarioDTO(funcionario));
            }
            model.addAttribute("funcionario", funcionario);
            model.addAttribute("activePage", "funcionarios");
            return "funcionarios/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/funcionarios";
        }
    }

    @PostMapping("/edit")
    public String editFuncionario(@RequestParam Integer id, @Valid @ModelAttribute("funcionarioDTO") FuncionarioDTO funcionarioDTO,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.funcionarioDTO", bindingResult);
            redirectAttributes.addFlashAttribute("funcionarioDTO", funcionarioDTO);
            return "redirect:/funcionarios/edit?id=" + id;
        }
        try {
            funcionarioService.atualizarFuncionario(id, funcionarioDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Funcionário atualizado com sucesso!");
            return "redirect:/funcionarios";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("funcionarioDTO", "documento", e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.funcionarioDTO", bindingResult);
            redirectAttributes.addFlashAttribute("funcionarioDTO", funcionarioDTO);
            return "redirect:/funcionarios/edit?id=" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/funcionarios";
        }
    }

    @GetMapping("/delete")
    public String deleteFuncionario(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            funcionarioService.deletarFuncionario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Funcionário excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // "Tradução" do erro
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o funcionário, pois ele está associado a uma ou mais Ordens de Serviço.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/funcionarios";
    }

    @GetMapping("/relatorio/pdf")
    public void gerarRelatorioPdf(@RequestParam(required = false) String keyword, HttpServletResponse response) throws IOException {
        // Busca a lista completa de funcionários
        List<Funcionario> funcionarios = funcionarioService.listarTodosParaRelatorio(keyword);

        // Prepara as variáveis para o template
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("funcionarios", funcionarios);
        variaveis.put("dataGeracao", LocalDateTime.now());
        variaveis.put("totalFuncionarios", funcionarios.size());

        // Gera o PDF
        byte[] pdfBytes = pdfService.gerarPdfDeHtml("funcionario-relatorio.html", variaveis);

        // Envia o PDF para o navegador
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_funcionarios.pdf");
        response.getOutputStream().write(pdfBytes);
    }

}