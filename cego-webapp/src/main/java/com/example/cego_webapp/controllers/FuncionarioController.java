package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.FuncionarioDTO;
import com.example.cego_webapp.models.Funcionario;
import com.example.cego_webapp.repositories.FuncionarioRepository;
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
@RequestMapping("/funcionarios")
public class FuncionarioController {
    @Autowired
    FuncionarioRepository funcionarioRepository;

    @GetMapping({"", "/"})
    public String getFuncionarios(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "13") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Funcionario> funcionariosPage = funcionarioRepository.findAll(pageable);

        model.addAttribute("funcionariosPage", funcionariosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", funcionariosPage.getTotalPages());

        return "funcionarios/index";
    }

    @GetMapping("/create")
    public String createFuncionario(Model model) {
        FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
        model.addAttribute("funcionarioDTO", funcionarioDTO);

        return "funcionarios/create";
    }

    @PostMapping("/create")
    public String createCliente(@Valid @ModelAttribute FuncionarioDTO funcionarioDTO, BindingResult bindingResult) {

        // Validação de documento único (com formatação removida)
        String documentoLimpo = funcionarioDTO.getDocumento().replaceAll("[^0-9]", "");
        if (funcionarioRepository.findByDocumento(documentoLimpo) != null) {
            bindingResult.addError(new FieldError(
                    "funcionarioDTO", "documento", "Documento já cadastrado"
            ));
        }

        if (bindingResult.hasErrors()) {
            return "funcionarios/create";
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setDocumento(documentoLimpo); // Salva sem formatação
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setTelefone(funcionarioDTO.getTelefone());
        funcionario.setEndereco(funcionarioDTO.getEndereco());
        funcionario.setCargo(funcionarioDTO.getCargo());
        funcionario.setSalario(funcionarioDTO.getSalario());

        funcionarioRepository.save(funcionario);

        return "redirect:/funcionarios/";
    }

    @GetMapping("/edit")
    public String editCliente(@RequestParam Integer id, Model model) {
        Funcionario funcionario = funcionarioRepository.findById(id).orElse(null);
        if (funcionario == null) {
            return "redirect:/funcionarios";
        }

        FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
        funcionarioDTO.setNome(funcionario.getNome());
        funcionarioDTO.setDocumento(funcionario.getDocumento());
        funcionarioDTO.setEmail(funcionario.getEmail());
        funcionarioDTO.setTelefone(funcionario.getTelefone());
        funcionarioDTO.setEndereco(funcionario.getEndereco());
        funcionarioDTO.setCargo(funcionario.getCargo());
        funcionarioDTO.setSalario(funcionario.getSalario());

        model.addAttribute("funcionario", funcionario);
        model.addAttribute("funcionarioDTO", funcionarioDTO);

        return "funcionarios/edit";

    }

    @PostMapping("/edit")
    public String editFuncionario(
            Model model,
            @RequestParam Integer id,
            @Valid @ModelAttribute FuncionarioDTO funcionarioDTO,
            BindingResult bindingResult
    ) {

        Funcionario funcionario = funcionarioRepository.findById(id).orElse(null);
        if (funcionario == null) {
            return "redirect:/funcionarios";
        }

        // Validação de documento único (se alterado)
        String documentoLimpo = funcionarioDTO.getDocumento().replaceAll("[^0-9]", "");
        if (!documentoLimpo.equals(funcionario.getDocumento())) {
            Funcionario existing = funcionarioRepository.findByDocumento(documentoLimpo);
            if (existing != null) {
                bindingResult.addError(new FieldError(
                        "funcionarioDTO", "documento", "Documento já cadastrado"
                ));
            }
        }

        model.addAttribute("funcionario", funcionario);

        if (bindingResult.hasErrors()) {
            model.addAttribute("funcionario", funcionario);
            return "funcionarios/edit";
        }

        // update
        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setDocumento(documentoLimpo);
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setTelefone(funcionarioDTO.getTelefone());
        funcionario.setEndereco(funcionarioDTO.getEndereco());
        funcionario.setCargo(funcionarioDTO.getCargo());
        funcionario.setSalario(funcionarioDTO.getSalario()); // Corrigido: usar DTO

        funcionarioRepository.save(funcionario);

        return "redirect:/funcionarios";
    }

    @GetMapping("/delete")
    public String deleteFuncionario(@RequestParam Integer id) {
        Funcionario funcionario = funcionarioRepository.findById(id).orElse(null);
        if (funcionario != null) {
            funcionarioRepository.delete(funcionario);
        }
        return "redirect:/funcionarios";
    }
}
