package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.FornecedorDTO;
import com.example.cego_webapp.models.Fornecedor;
import com.example.cego_webapp.repositories.FornecedorRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Adicionar import

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {
    @Autowired
    private FornecedorRepository fornecedorRepository;

    @GetMapping({"", "/"})
    public String getFornecedores(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "id,desc") String sort, // Adicionado para manter o estado da ordenação
                                  @RequestParam(required = false) String keyword) { // Adicionado para manter o estado da busca

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));
        Page<Fornecedor> fornecedoresPage;

        if(keyword != null && !keyword.isEmpty()){
            fornecedoresPage = fornecedorRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            fornecedoresPage = fornecedorRepository.findAll(pageable);
        }

        model.addAttribute("fornecedoresPage", fornecedoresPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", fornecedoresPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);

        return "fornecedores/index";
    }

    @GetMapping("/create")
    public String createFornecedor(Model model) {
        if (!model.containsAttribute("fornecedorDTO")) {
            model.addAttribute("fornecedorDTO", new FornecedorDTO());
        }
        return "fornecedores/create";
    }

    @PostMapping("/create")
    public String createFornecedor(@Valid @ModelAttribute("fornecedorDTO") FornecedorDTO fornecedorDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        String documentoLimpo = fornecedorDTO.getDocumento().replaceAll("[^0-9]", "");
        if (fornecedorRepository.findByDocumento(documentoLimpo) != null) {
            bindingResult.addError(new FieldError("fornecedorDTO", "documento", "Documento já cadastrado"));
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fornecedorDTO", bindingResult);
            redirectAttributes.addFlashAttribute("fornecedorDTO", fornecedorDTO);
            return "redirect:/fornecedores/create";
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(fornecedorDTO.getNome());
        fornecedor.setDocumento(documentoLimpo);
        fornecedor.setEmail(fornecedorDTO.getEmail());
        fornecedor.setTelefone(fornecedorDTO.getTelefone());
        fornecedor.setEndereco(fornecedorDTO.getEndereco());
        fornecedorRepository.save(fornecedor);

        redirectAttributes.addFlashAttribute("successMessage", "Fornecedor cadastrado com sucesso!");
        return "redirect:/fornecedores";
    }

    @GetMapping("/edit")
    public String editFornecedor(@RequestParam Integer id, Model model) {
        Fornecedor fornecedor = fornecedorRepository.findById(id).orElse(null);
        if (fornecedor == null) {
            return "redirect:/fornecedores";
        }

        if (!model.containsAttribute("fornecedorDTO")) {
            FornecedorDTO fornecedorDTO = new FornecedorDTO();
            fornecedorDTO.setNome(fornecedor.getNome());
            fornecedorDTO.setDocumento(fornecedor.getDocumento());
            fornecedorDTO.setEmail(fornecedor.getEmail());
            fornecedorDTO.setTelefone(fornecedor.getTelefone());
            fornecedorDTO.setEndereco(fornecedor.getEndereco());
            model.addAttribute("fornecedorDTO", fornecedorDTO);
        }

        model.addAttribute("fornecedor", fornecedor);
        return "fornecedores/edit";
    }

    @PostMapping("/edit")
    public String editFornecedor(@RequestParam Integer id, @Valid @ModelAttribute("fornecedorDTO") FornecedorDTO fornecedorDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Fornecedor fornecedor = fornecedorRepository.findById(id).orElse(null);
        if (fornecedor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Fornecedor não encontrado.");
            return "redirect:/fornecedores";
        }

        String documentoLimpo = fornecedorDTO.getDocumento().replaceAll("[^0-9]", "");
        if (!documentoLimpo.equals(fornecedor.getDocumento())) {
            Fornecedor existing = fornecedorRepository.findByDocumento(documentoLimpo);
            if (existing != null) {
                bindingResult.addError(new FieldError("fornecedorDTO", "documento", "Documento já cadastrado"));
            }
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fornecedorDTO", bindingResult);
            redirectAttributes.addFlashAttribute("fornecedorDTO", fornecedorDTO);
            return "redirect:/fornecedores/edit?id=" + id;
        }

        fornecedor.setNome(fornecedorDTO.getNome());
        fornecedor.setDocumento(documentoLimpo);
        fornecedor.setEmail(fornecedorDTO.getEmail());
        fornecedor.setTelefone(fornecedorDTO.getTelefone());
        fornecedor.setEndereco(fornecedorDTO.getEndereco());
        fornecedorRepository.save(fornecedor);

        redirectAttributes.addFlashAttribute("successMessage", "Fornecedor atualizado com sucesso!");
        return "redirect:/fornecedores";
    }

    @GetMapping("/delete")
    public String deleteFornecedor(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            if (!fornecedorRepository.existsById(id)) {
                throw new Exception("Fornecedor não encontrado.");
            }
            fornecedorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Fornecedor excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o fornecedor, pois ele está associado a uma ou mais compras.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir o fornecedor: " + e.getMessage());
        }
        return "redirect:/fornecedores";
    }
}