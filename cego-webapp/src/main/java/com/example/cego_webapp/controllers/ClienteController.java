// src/main/java/com/example/cego_webapp/controllers/ClienteController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ClienteDTO;
import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.services.ClienteService;
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
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PdfService pdfService;

    // ... (get, create e post create continuam iguais) ...
    @GetMapping({"", "/"})
    public String getClientes(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id,desc") String sort, @RequestParam(required = false) String keyword) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));
        Page<Cliente> clientesPage = clienteService.listarClientes(keyword, pageable);
        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientesPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "clientes");
        return "clientes/index";
    }
    @GetMapping("/create")
    public String createClienteForm(Model model) {
        if (!model.containsAttribute("clienteDTO")) {
            model.addAttribute("clienteDTO", new ClienteDTO());
        }
        model.addAttribute("activePage", "clientes");
        return "clientes/create";
    }
    @PostMapping("/create")
    public String createCliente(@Valid @ModelAttribute("clienteDTO") ClienteDTO clienteDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.clienteDTO", bindingResult);
                redirectAttributes.addFlashAttribute("clienteDTO", clienteDTO);
                return "redirect:/clientes/create";
            }
            clienteService.criarCliente(clienteDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente cadastrado com sucesso!");
            return "redirect:/clientes";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("clienteDTO", "documento", e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.clienteDTO", bindingResult);
            redirectAttributes.addFlashAttribute("clienteDTO", clienteDTO);
            return "redirect:/clientes/create";
        }
    }


    // ### MÉTODO CORRIGIDO ###
    @GetMapping("/edit")
    public String editClienteForm(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.buscarPorId(id);
            if (!model.containsAttribute("clienteDTO")) {
                model.addAttribute("clienteDTO", new ClienteDTO(cliente));
            }

            // CORREÇÃO: Enviar o objeto 'cliente' inteiro para a view
            model.addAttribute("cliente", cliente);

            model.addAttribute("activePage", "clientes");
            return "clientes/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/clientes";
        }
    }

    // ... (post edit e delete continuam iguais) ...
    @PostMapping("/edit")
    public String editCliente(@RequestParam Integer id, @Valid @ModelAttribute("clienteDTO") ClienteDTO clienteDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.clienteDTO", bindingResult);
            redirectAttributes.addFlashAttribute("clienteDTO", clienteDTO);
            return "redirect:/clientes/edit?id=" + id;
        }
        try {
            clienteService.atualizarCliente(id, clienteDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente atualizado com sucesso!");
            return "redirect:/clientes";
        } catch (IllegalArgumentException e) {
            bindingResult.addError(new FieldError("clienteDTO", "documento", e.getMessage()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.clienteDTO", bindingResult);
            redirectAttributes.addFlashAttribute("clienteDTO", clienteDTO);
            return "redirect:/clientes/edit?id=" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/clientes";
        }
    }

    @GetMapping("/delete")
    public String deleteCliente(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.deletarCliente(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // ### A "TRADUÇÃO" DO ERRO ACONTECE AQUI ###
            // Capturamos o erro específico do banco de dados e criamos uma mensagem amigável.
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir este cliente, pois ele está associado a Vendas, Veículos ou Ordens de Serviço.");
        } catch (Exception e) {
            // Captura qualquer outro erro (como "Não encontrado")
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/relatorio/pdf")
    public void gerarRelatorioPdf(@RequestParam(required = false) String keyword, HttpServletResponse response) throws IOException {

        // CORREÇÃO: Chamar o novo método que retorna a lista COMPLETA
        List<Cliente> clientes = clienteService.listarTodosParaRelatorio(keyword);

        // O resto do método permanece o mesmo
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("clientes", clientes);
        variaveis.put("dataGeracao", LocalDateTime.now());

        variaveis.put("totalClientes", clientes.size());

        byte[] pdfBytes = pdfService.gerarPdfDeHtml("cliente-relatorio.html", variaveis);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_clientes.pdf");
        response.getOutputStream().write(pdfBytes);
    }
}