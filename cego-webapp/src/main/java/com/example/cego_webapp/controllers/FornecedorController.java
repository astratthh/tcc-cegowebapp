package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.FornecedorDTO;
import com.example.cego_webapp.models.Fornecedor;
import com.example.cego_webapp.repositories.FornecedorRepository;
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
@RequestMapping("/fornecedores")
public class FornecedorController {
    @Autowired
    private FornecedorRepository fornecedorRepository;

    @GetMapping({"", "/"})
    public String getFornecedores(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "13") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Fornecedor> fornecedoresPage = fornecedorRepository.findAll(pageable);

        model.addAttribute("fornecedoresPage", fornecedoresPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", fornecedoresPage.getTotalPages());

        return "fornecedores/index";
    }


    @GetMapping("/create")
    public String createFornecedor(Model model) {
        FornecedorDTO fornecedorDTO = new FornecedorDTO();
        model.addAttribute("fornecedorDTO", fornecedorDTO);

        return "fornecedores/create";
    }

    @PostMapping("/create")
    public String createFornecedor(@Valid @ModelAttribute FornecedorDTO fornecedorDTO, BindingResult bindingResult) {

        String documentoLimpo = fornecedorDTO.getDocumento().replaceAll("[^0-9]", "");
        if (fornecedorRepository.findByDocumento(documentoLimpo) != null) {
            bindingResult.addError(new FieldError(
                    "fornecedorDTO", "documento", "Documento já cadastrado"
            ));
        }

        if (bindingResult.hasErrors()) {
            return "fornecedores/create";
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(fornecedorDTO.getNome());
        fornecedor.setDocumento(documentoLimpo);
        fornecedor.setEmail(fornecedorDTO.getEmail());
        fornecedor.setTelefone(fornecedorDTO.getTelefone());
        fornecedor.setEndereco(fornecedorDTO.getEndereco());
        fornecedorRepository.save(fornecedor);

        return "redirect:/fornecedores/";
    }

    @GetMapping("/edit")
    public String editFornecedor(@RequestParam Integer id, Model model) {
        Fornecedor fornecedor = fornecedorRepository.findById(id).orElse(null);
        if (fornecedor == null) {
            return "redirect:/fornecedores";
        }

        FornecedorDTO fornecedorDTO = new FornecedorDTO();
        fornecedorDTO.setNome(fornecedor.getNome());
        fornecedorDTO.setDocumento(fornecedor.getDocumento());
        fornecedorDTO.setEmail(fornecedor.getEmail());
        fornecedorDTO.setTelefone(fornecedor.getTelefone());
        fornecedorDTO.setEndereco(fornecedor.getEndereco());

        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("fornecedorDTO", fornecedorDTO);

        return "fornecedores/edit";

    }

    @PostMapping("/edit")
    public String editFornecedor(
            Model model,
            @RequestParam Integer id,
            @Valid @ModelAttribute FornecedorDTO fornecedorDTO,
            BindingResult bindingResult
    ) {

        Fornecedor fornecedor = fornecedorRepository.findById(id).orElse(null);
        if (fornecedor == null) {
            return "redirect:/fornecedores";
        }

        String documentoLimpo = fornecedorDTO.getDocumento().replaceAll("[^0-9]", "");
        if (!documentoLimpo.equals(fornecedor.getDocumento())) {
            Fornecedor existing = fornecedorRepository.findByDocumento(documentoLimpo);
            if (existing != null) {
                bindingResult.addError(new FieldError(
                        "fornecedorDTO", "documento", "Documento já cadastrado"
                ));
            }
        }

        model.addAttribute("fornecedor", fornecedor);

        if (bindingResult.hasErrors()) {
            model.addAttribute("fornecedor", fornecedor);
            return "fornecedores/edit";
        }

        fornecedor.setNome(fornecedorDTO.getNome());
        fornecedor.setDocumento(documentoLimpo);
        fornecedor.setEmail(fornecedorDTO.getEmail());
        fornecedor.setTelefone(fornecedorDTO.getTelefone());
        fornecedor.setEndereco(fornecedorDTO.getEndereco());
        fornecedorRepository.save(fornecedor);

        return "redirect:/fornecedores";
    }

    @GetMapping("/delete")
    public String deleteFornecedor(@RequestParam Integer id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id).orElse(null);
        if (fornecedor != null) {
            fornecedorRepository.delete(fornecedor);
        }
        return "redirect:/fornecedores";
    }
}
