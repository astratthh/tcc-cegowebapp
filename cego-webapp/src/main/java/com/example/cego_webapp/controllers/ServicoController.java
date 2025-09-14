package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ServicoDTO;
import com.example.cego_webapp.models.Servico;
import com.example.cego_webapp.repositories.ServicoRepository;
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

@Controller
@RequestMapping("/servicos")
public class ServicoController {

    @Autowired
    private ServicoRepository servicoRepository;

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

        Page<Servico> servicosPage;
        if (keyword != null && !keyword.isEmpty()) {
            servicosPage = servicoRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            servicosPage = servicoRepository.findAll(pageable);
        }

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
    public String createServico(Model model) {
        if (!model.containsAttribute("servicoDTO")) {
            model.addAttribute("servicoDTO", new ServicoDTO());
        }
        model.addAttribute("activePage", "servicos");
        return "servicos/create";
    }

    @PostMapping("/create")
    public String createServico(@Valid @ModelAttribute("servicoDTO") ServicoDTO servicoDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.servicoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("servicoDTO", servicoDTO);
            return "redirect:/servicos/create";
        }

        Servico servico = new Servico();
        servico.setNome(servicoDTO.getNome());
        servico.setDescricao(servicoDTO.getDescricao());
        servico.setPreco(servicoDTO.getPreco());
        servicoRepository.save(servico);

        redirectAttributes.addFlashAttribute("successMessage", "Serviço cadastrado com sucesso!");
        return "redirect:/servicos";
    }

    @GetMapping("/edit")
    public String editServico(@RequestParam Integer id, Model model) {
        Servico servico = servicoRepository.findById(id).orElse(null);
        if (servico == null) {
            return "redirect:/servicos";
        }

        if (!model.containsAttribute("servicoDTO")) {
            ServicoDTO servicoDTO = new ServicoDTO();
            servicoDTO.setNome(servico.getNome());
            servicoDTO.setDescricao(servico.getDescricao());
            servicoDTO.setPreco(servico.getPreco());
            model.addAttribute("servicoDTO", servicoDTO);
        }

        model.addAttribute("servico", servico);
        model.addAttribute("activePage", "servicos");
        return "servicos/edit";
    }

    @PostMapping("/edit")
    public String editServico(@RequestParam Integer id, @Valid @ModelAttribute("servicoDTO") ServicoDTO servicoDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Servico servico = servicoRepository.findById(id).orElse(null);
        if (servico == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Serviço não encontrado.");
            return "redirect:/servicos";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.servicoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("servicoDTO", servicoDTO);
            return "redirect:/servicos/edit?id=" + id;
        }

        servico.setNome(servicoDTO.getNome());
        servico.setDescricao(servicoDTO.getDescricao());
        servico.setPreco(servicoDTO.getPreco());
        servicoRepository.save(servico);

        redirectAttributes.addFlashAttribute("successMessage", "Serviço atualizado com sucesso!");
        return "redirect:/servicos";
    }

    @GetMapping("/delete")
    public String deleteServico(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            if (!servicoRepository.existsById(id)) {
                throw new Exception("Serviço não encontrado.");
            }
            servicoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Serviço excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o serviço, pois ele está associado a uma ou mais Ordens de Serviço.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir o serviço: " + e.getMessage());
        }
        return "redirect:/servicos";
    }
}