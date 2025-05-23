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

//    @GetMapping({"", "/"})
//    public String getClientes(Model model) {
//        var clientes = clienteRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
//        model.addAttribute("clientes", clientes);
//
//        return "clientes/index";
//    }

    @GetMapping({"", "/"})
    public String getClientes(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "13") int size) {
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

        if (clienteRepository.findByEmail(clienteDTO.getEmail()) != null) {
            bindingResult.addError(
                    new FieldError("clienteDTO", "email", clienteDTO.getEmail()
                    , false, null, null, "Email em uso")
            );
        }

        if (bindingResult.hasErrors()) {
            return "clientes/create";
        }

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDTO.getNome());
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

        model.addAttribute("cliente", cliente);

        if (bindingResult.hasErrors()) {
            return "clientes/edit";
        }

        // update
        cliente.setNome(clienteDTO.getNome());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefone(clienteDTO.getTelefone());
        cliente.setEndereco(clienteDTO.getEndereco());

        try {
            clienteRepository.save(cliente);
        }
        catch (Exception e) {
            bindingResult.addError(
                    new FieldError("clienteDTO", "email", clienteDTO.getEmail()
                    , false, null, null, "Email em uso")
            );
            return "clientes/edit";
        }

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



//package com.example.cego_webapp.controllers;
//
//import com.example.cego_webapp.dto.ClienteDTO;
//import com.example.cego_webapp.models.Cliente;
//import com.example.cego_webapp.repositories.ClienteRepository;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/clientes")
//public class ClienteController {
//
//    @Autowired
//    private ClienteRepository clienteRepository;
//
//    @GetMapping({"", "/"})
//    public String getClientes(Model model,
//                              @RequestParam(defaultValue = "0") int page,
//                              @RequestParam(defaultValue = "13") int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
//        Page<Cliente> clientesPage = clienteRepository.findAll(pageable);
//
//        model.addAttribute("clientesPage", clientesPage);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", clientesPage.getTotalPages());
//
//        return "clientes/index";
//    }
//
//    @GetMapping("/create")
//    public String showCreateForm(Model model) {
//        model.addAttribute("clienteDTO", new ClienteDTO());
//        return "clientes/create :: form";
//    }
//
//    @PostMapping("/create")
//    public String createCliente(@Valid @ModelAttribute ClienteDTO clienteDTO,
//                                BindingResult bindingResult) {
//
//        if (clienteRepository.findByEmail(clienteDTO.getEmail()) != null) {
//            bindingResult.addError(new FieldError("clienteDTO", "email", "Email já está em uso"));
//        }
//
//        if (bindingResult.hasErrors()) {
//            return "clientes/create :: form";
//        }
//
//        Cliente cliente = new Cliente();
//        cliente.setNome(clienteDTO.getNome());
//        cliente.setEmail(clienteDTO.getEmail());
//        cliente.setTelefone(clienteDTO.getTelefone());
//        cliente.setEndereco(clienteDTO.getEndereco());
//        clienteRepository.save(cliente);
//
//        return "redirect:/clientes/";
//    }
//
//    @GetMapping("/edit")
//    public String showEditForm(@RequestParam Integer id, Model model) {
//        Cliente cliente = clienteRepository.findById(id).orElse(null);
//        if (cliente == null) {
//            return "redirect:/clientes";
//        }
//
//        ClienteDTO clienteDTO = new ClienteDTO();
//        clienteDTO.setNome(cliente.getNome());
//        clienteDTO.setEmail(cliente.getEmail());
//        clienteDTO.setTelefone(cliente.getTelefone());
//        clienteDTO.setEndereco(cliente.getEndereco());
//
//        model.addAttribute("clienteDTO", clienteDTO);
//        model.addAttribute("clienteId", id);
//
//        return "clientes/edit :: form";
//    }
//
//    @PostMapping("/edit")
//    public String updateCliente(@RequestParam Integer id,
//                                @Valid @ModelAttribute ClienteDTO clienteDTO,
//                                BindingResult bindingResult) {
//
//        Cliente cliente = clienteRepository.findById(id).orElse(null);
//        if (cliente == null) {
//            return "redirect:/clientes";
//        }
//
//        // Verifica se o email foi alterado
//        if (!cliente.getEmail().equals(clienteDTO.getEmail()) &&
//                clienteRepository.findByEmail(clienteDTO.getEmail()) != null) {
//            bindingResult.addError(new FieldError("clienteDTO", "email", "Email já está em uso"));
//        }
//
//        if (bindingResult.hasErrors()) {
//            return "clientes/edit :: form";
//        }
//
//        cliente.setNome(clienteDTO.getNome());
//        cliente.setEmail(clienteDTO.getEmail());
//        cliente.setTelefone(clienteDTO.getTelefone());
//        cliente.setEndereco(clienteDTO.getEndereco());
//        clienteRepository.save(cliente);
//
//        return "redirect:/clientes/";
//    }
//
//    @GetMapping("/delete")
//    public String deleteCliente(@RequestParam Integer id) {
//        clienteRepository.deleteById(id);
//        return "redirect:/clientes";
//    }
//}


