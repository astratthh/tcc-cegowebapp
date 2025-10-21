// src/main/java/com/example/cego_webapp/controllers/ServicoController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ServicoDTO;
import com.example.cego_webapp.models.Servico;
import com.example.cego_webapp.services.PdfService;
import com.example.cego_webapp.services.ServicoService; // NOVO IMPORT
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/servicos")
public class ServicoController {

    @Autowired
    private ServicoService servicoService; // INJETA O SERVICE

    @Autowired
    private PdfService pdfService;

    @GetMapping({"", "/"})
    public String getServicos(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id,desc") String sort,
                              @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        Page<Servico> servicosPage = servicoService.listarServicos(keyword, pageable);

        model.addAttribute("servicosPage", servicosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicosPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "servicos");

        return "servicos/index";
    }

    @GetMapping("/create")
    public String createServicoForm(Model model) {
        if (!model.containsAttribute("servicoDTO")) {
            model.addAttribute("servicoDTO", new ServicoDTO());
        }
        model.addAttribute("activePage", "servicos");
        return "servicos/create";
    }

    @PostMapping("/create")
    public String createServico(@Valid @ModelAttribute("servicoDTO") ServicoDTO servicoDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.servicoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("servicoDTO", servicoDTO);
            return "redirect:/servicos/create";
        }
        servicoService.criarServico(servicoDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Serviço cadastrado com sucesso!");
        return "redirect:/servicos";
    }

    @GetMapping("/edit")
    public String editServicoForm(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Servico servico = servicoService.buscarPorId(id);
            if (!model.containsAttribute("servicoDTO")) {
                // Adicione este construtor ao seu ServicoDTO
                model.addAttribute("servicoDTO", new ServicoDTO(servico));
            }
            model.addAttribute("servico", servico);
            model.addAttribute("activePage", "servicos");
            return "servicos/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/servicos";
        }
    }

    @PostMapping("/edit")
    public String editServico(@RequestParam Integer id, @Valid @ModelAttribute("servicoDTO") ServicoDTO servicoDTO,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.servicoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("servicoDTO", servicoDTO);
            return "redirect:/servicos/edit?id=" + id;
        }
        try {
            servicoService.atualizarServico(id, servicoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Serviço atualizado com sucesso!");
            return "redirect:/servicos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("servicoDTO", servicoDTO);
            return "redirect:/servicos/edit?id=" + id;
        }
    }

    @GetMapping("/delete")
    public String deleteServico(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            servicoService.deletarServico(id);
            redirectAttributes.addFlashAttribute("successMessage", "Serviço excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // "Tradução" do erro
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o serviço, pois ele está associado a uma ou mais Ordens de Serviço.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/servicos";
    }

    @GetMapping("/relatorio/pdf")
    public void gerarRelatorioPdf(@RequestParam(required = false) String keyword, HttpServletResponse response) throws IOException {
        List<Servico> servicos = servicoService.listarTodosParaRelatorio(keyword);
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("servicos", servicos);
        variaveis.put("dataGeracao", LocalDateTime.now());
        variaveis.put("totalServicos", servicos.size());

        byte[] pdfBytes = pdfService.gerarPdfDeHtml("servico-relatorio.html", variaveis);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_servicos.pdf");
        response.getOutputStream().write(pdfBytes);
    }
}