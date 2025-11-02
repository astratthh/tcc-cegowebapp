// src/main/java/com/example/cego_webapp/controllers/VeiculoController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.VeiculoDTO;
import com.example.cego_webapp.models.Veiculo;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.services.PdfService;
import com.example.cego_webapp.services.VeiculoService; // NOVO IMPORT
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
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping({"", "/"})
    public String getVeiculos(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id,desc") String sort,
                              @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        Page<Veiculo> veiculosPage = veiculoService.listarVeiculos(keyword, pageable);

        model.addAttribute("veiculosPage", veiculosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", veiculosPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);

        return "veiculos/index";
    }

    @GetMapping("/create")
    public String createVeiculoForm(Model model) {
        if (!model.containsAttribute("veiculoDTO")) {
            model.addAttribute("veiculoDTO", new VeiculoDTO());
        }
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
        return "veiculos/create";
    }

    @PostMapping("/create")
    public String createVeiculo(@Valid @ModelAttribute("veiculoDTO") VeiculoDTO veiculoDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.veiculoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("veiculoDTO", veiculoDTO);
            return "redirect:/veiculos/create";
        }
        try {
            veiculoService.criarVeiculo(veiculoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Veículo cadastrado com sucesso!");
            return "redirect:/veiculos";
        } catch (IllegalArgumentException e) {
            // Captura erros de negócio (placa, cliente) do service
            String field = e.getMessage().toLowerCase().contains("placa") ? "placa" : "clienteId";
            bindingResult.addError(new FieldError("veiculoDTO", field, e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.veiculoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("veiculoDTO", veiculoDTO);
            return "redirect:/veiculos/create";
        }
    }

    @GetMapping("/edit")
    public String editVeiculoForm(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Veiculo veiculo = veiculoService.buscarPorId(id);
            if (!model.containsAttribute("veiculoDTO")) {
                model.addAttribute("veiculoDTO", new VeiculoDTO(veiculo));
            }
            model.addAttribute("veiculo", veiculo);
            model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
            return "veiculos/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/veiculos";
        }
    }

    @PostMapping("/edit")
    public String editVeiculo(@RequestParam Integer id, @Valid @ModelAttribute("veiculoDTO") VeiculoDTO veiculoDTO,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.veiculoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("veiculoDTO", veiculoDTO);
            return "redirect:/veiculos/edit?id=" + id;
        }
        try {
            veiculoService.atualizarVeiculo(id, veiculoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Veículo atualizado com sucesso!");
            return "redirect:/veiculos";
        } catch (IllegalArgumentException e) {
            String field = e.getMessage().toLowerCase().contains("placa") ? "placa" : "clienteId";
            bindingResult.addError(new FieldError("veiculoDTO", field, e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.veiculoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("veiculoDTO", veiculoDTO);
            return "redirect:/veiculos/edit?id=" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/veiculos";
        }
    }

    @GetMapping("/api/por-cliente/{clienteId}")
    @ResponseBody
    public List<Veiculo> getVeiculosPorCliente(@PathVariable Integer clienteId) {
        return veiculoService.listarPorCliente(clienteId);
    }

    @GetMapping("/delete")
    public String deleteVeiculo(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            veiculoService.deletarVeiculo(id);
            redirectAttributes.addFlashAttribute("successMessage", "Veículo excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // "Tradução" do erro
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o veículo, pois ele está associado a uma ou mais Ordens de Serviço.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/veiculos";
    }

    @GetMapping("/relatorio/pdf")
    public void gerarRelatorioPdf(@RequestParam(required = false) String keyword, HttpServletResponse response) throws IOException {
        List<Veiculo> veiculos = veiculoService.listarTodosParaRelatorio(keyword);
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("veiculos", veiculos);
        variaveis.put("dataGeracao", LocalDateTime.now());
        variaveis.put("totalVeiculos", veiculos.size());

        byte[] pdfBytes = pdfService.gerarPdfDeHtml("veiculo-relatorio.html", variaveis);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_veiculos.pdf");
        response.getOutputStream().write(pdfBytes);
    }

}