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
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "id,desc") String sort,
                                  @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        Page<Funcionario> funcionariosPage;
        if (keyword != null && !keyword.isEmpty()) {
            funcionariosPage = funcionarioRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            funcionariosPage = funcionarioRepository.findAll(pageable);
        }

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
    public String createFuncionario(Model model) {
        FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
        model.addAttribute("funcionarioDTO", funcionarioDTO);
        model.addAttribute("activePage", "funcionarios");
        return "funcionarios/create";
    }

    @PostMapping("/create")
    public String createFuncionario(@Valid @ModelAttribute FuncionarioDTO funcionarioDTO, BindingResult bindingResult, Model model) {
        String documentoLimpo = funcionarioDTO.getDocumento().replaceAll("[^0-9]", "");
        if (funcionarioRepository.findByDocumento(documentoLimpo) != null) {
            bindingResult.addError(new FieldError("funcionarioDTO", "documento", "Documento já cadastrado"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "funcionarios");
            return "funcionarios/create";
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setDocumento(documentoLimpo);
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setTelefone(funcionarioDTO.getTelefone());
        funcionario.setEndereco(funcionarioDTO.getEndereco());
        funcionario.setCargo(funcionarioDTO.getCargo());
        funcionario.setSalario(funcionarioDTO.getSalario());
        funcionarioRepository.save(funcionario);

        return "redirect:/funcionarios/";
    }

    @GetMapping("/edit")
    public String editFuncionario(@RequestParam Integer id, Model model) {
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
        model.addAttribute("activePage", "funcionarios");

        return "funcionarios/edit";
    }

    @PostMapping("/edit")
    public String editFuncionario(Model model, @RequestParam Integer id, @Valid @ModelAttribute FuncionarioDTO funcionarioDTO, BindingResult bindingResult) {
        Funcionario funcionario = funcionarioRepository.findById(id).orElse(null);
        if (funcionario == null) {
            return "redirect:/funcionarios";
        }

        String documentoLimpo = funcionarioDTO.getDocumento().replaceAll("[^0-9]", "");
        Funcionario existing = funcionarioRepository.findByDocumento(documentoLimpo);
        if (existing != null && !existing.getId().equals(id)) {
            bindingResult.addError(new FieldError("funcionarioDTO", "documento", "Documento já cadastrado em outro funcionário"));
        }

        model.addAttribute("funcionario", funcionario);

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "funcionarios");
            return "funcionarios/edit";
        }

        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setDocumento(documentoLimpo);
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setTelefone(funcionarioDTO.getTelefone());
        funcionario.setEndereco(funcionarioDTO.getEndereco());
        funcionario.setCargo(funcionarioDTO.getCargo());
        funcionario.setSalario(funcionarioDTO.getSalario());
        funcionarioRepository.save(funcionario);

        return "redirect:/funcionarios";
    }

    @GetMapping("/delete")
    public String deleteFuncionario(@RequestParam Integer id) {
        funcionarioRepository.deleteById(id);
        return "redirect:/funcionarios";
    }
}