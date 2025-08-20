package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ClienteDTO;
import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.repositories.ClienteRepository;
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

@Controller
@RequestMapping("/clientes")
public class ClienteController {
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping({"", "/"})
    public String getClientes(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Cliente> clientesPage = clienteRepository.findAll(pageable);

        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientesPage.getTotalPages());

        return "clientes/index";
    }


    @GetMapping("/create")
    public String createCliente(Model model) {
        ClienteDTO clienteDTO = new ClienteDTO();
        model.addAttribute("clienteDTO", clienteDTO);

        return "clientes/create";
    }

    @PostMapping("/create")
    public String createCliente(@Valid @ModelAttribute ClienteDTO clienteDTO, BindingResult bindingResult) {

        // Validação de documento único (com formatação removida)
        String documentoLimpo = clienteDTO.getDocumento().replaceAll("[^0-9]", "");
        if (clienteRepository.findByDocumento(documentoLimpo) != null) {
            bindingResult.addError(new FieldError(
                    "clienteDTO", "documento", "Documento já cadastrado"
            ));
        }

        if (bindingResult.hasErrors()) {
            return "clientes/create";
        }

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDTO.getNome());
        cliente.setDocumento(documentoLimpo); // Salva sem formatação
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefone(clienteDTO.getTelefone());
        cliente.setEndereco(clienteDTO.getEndereco());
        clienteRepository.save(cliente);

        return "redirect:/clientes/";
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

        return "clientes/edit";

    }

    @PostMapping("/edit")
    public String editCliente(
            Model model,
            @RequestParam Integer id,
            @Valid @ModelAttribute ClienteDTO clienteDTO,
            BindingResult bindingResult
    ) {

        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            return "redirect:/clientes";
        }

        // Validação de documento único (se alterado)
        String documentoLimpo = clienteDTO.getDocumento().replaceAll("[^0-9]", "");
        if (!documentoLimpo.equals(cliente.getDocumento())) {
            Cliente existing = clienteRepository.findByDocumento(documentoLimpo);
            if (existing != null) {
                bindingResult.addError(new FieldError(
                        "clienteDTO", "documento", "Documento já cadastrado"
                ));
            }
        }

        model.addAttribute("cliente", cliente);

        if (bindingResult.hasErrors()) {
            model.addAttribute("cliente", cliente);
            return "clientes/edit";
        }

        // update
        cliente.setNome(clienteDTO.getNome());;
        cliente.setDocumento(documentoLimpo); // Salva sem formatação
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefone(clienteDTO.getTelefone());
        cliente.setEndereco(clienteDTO.getEndereco());
        clienteRepository.save(cliente);

        return "redirect:/clientes";
    }

    @GetMapping("/delete")
    public String deleteCliente(@RequestParam Integer id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null) {
            clienteRepository.delete(cliente);
        }
        return "redirect:/clientes";
    }

}