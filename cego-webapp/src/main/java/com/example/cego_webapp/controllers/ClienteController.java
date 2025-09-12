package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ClienteDTO;
import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.repositories.ClienteRepository;
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

@Controller
@RequestMapping("/clientes")
public class ClienteController {
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping({"", "/"})
    public String getClientes(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id,desc") String sort,
                              @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        Page<Cliente> clientesPage;
        if (keyword != null && !keyword.isEmpty()) {
            clientesPage = clienteRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            clientesPage = clienteRepository.findAll(pageable);
        }

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
    public String createCliente(Model model) {
        // Garante que o DTO esteja presente na página, especialmente após um erro de validação
        if (!model.containsAttribute("clienteDTO")) {
            model.addAttribute("clienteDTO", new ClienteDTO());
        }
        model.addAttribute("activePage", "clientes");

        // CORREÇÃO: Deve retornar a view de criação, e não a de listagem
        return "clientes/create";
    }

    @PostMapping("/create")
    public String createCliente(@Valid @ModelAttribute("clienteDTO") ClienteDTO clienteDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        String documentoLimpo = clienteDTO.getDocumento() != null ? clienteDTO.getDocumento().replaceAll("[^0-9]", "") : "";
        if (clienteRepository.findByDocumento(documentoLimpo) != null) {
            bindingResult.addError(new FieldError("clienteDTO", "documento", "Documento já cadastrado."));
        }

        if (bindingResult.hasErrors()) {
            // BOA PRÁTICA: Usar RedirectAttributes para passar os erros e os dados de volta para o formulário
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.clienteDTO", bindingResult);
            redirectAttributes.addFlashAttribute("clienteDTO", clienteDTO);
            return "redirect:/clientes/create";
        }

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDTO.getNome());
        cliente.setDocumento(documentoLimpo);
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefone(clienteDTO.getTelefone());
        cliente.setEndereco(clienteDTO.getEndereco());
        clienteRepository.save(cliente);

        redirectAttributes.addFlashAttribute("successMessage", "Cliente cadastrado com sucesso!");
        return "redirect:/clientes";
    }

    @GetMapping("/edit")
    public String editCliente(@RequestParam Integer id, Model model) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            return "redirect:/clientes";
        }

        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome(cliente.getNome());
        clienteDTO.setDocumento(cliente.getDocumento());
        clienteDTO.setEmail(cliente.getEmail());
        clienteDTO.setTelefone(cliente.getTelefone());
        clienteDTO.setEndereco(cliente.getEndereco());

        model.addAttribute("cliente", cliente);
        model.addAttribute("clienteDTO", clienteDTO);
        model.addAttribute("activePage", "clientes"); // Para a sidebar

        return "clientes/edit";
    }

    @PostMapping("/edit")
    public String editCliente(Model model, @RequestParam Integer id, @Valid @ModelAttribute ClienteDTO clienteDTO, BindingResult bindingResult) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            return "redirect:/clientes";
        }

        String documentoLimpo = clienteDTO.getDocumento().replaceAll("[^0-9]", "");
        Cliente existing = clienteRepository.findByDocumento(documentoLimpo);
        if (existing != null && !existing.getId().equals(id)) {
            bindingResult.addError(new FieldError("clienteDTO", "documento", "Documento já cadastrado em outro cliente"));
        }

        model.addAttribute("cliente", cliente);
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "clientes"); // Para a sidebar em caso de erro
            return "clientes/edit";
        }

        cliente.setNome(clienteDTO.getNome());
        cliente.setDocumento(documentoLimpo);
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefone(clienteDTO.getTelefone());
        cliente.setEndereco(clienteDTO.getEndereco());
        clienteRepository.save(cliente);

        return "redirect:/clientes";
    }

    @GetMapping("/delete")
    public String deleteCliente(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteRepository.findById(id).orElseThrow(null);
            clienteRepository.delete(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir este cliente, pois ele está associado a outros registros.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir o cliente.");
        }
        return "redirect:/clientes";
    }
}