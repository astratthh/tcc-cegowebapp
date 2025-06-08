package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ServicoDTO;
import com.example.cego_webapp.models.Servico;
import com.example.cego_webapp.repositories.ServicoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/servicos")
public class ServicoController {

    @Autowired
    private ServicoRepository servicoRepository;

    @GetMapping({"", "/"})
    public String getServicos(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "13") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Servico> servicosPage = servicoRepository.findAll(pageable);

        model.addAttribute("servicosPage", servicosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicosPage.getTotalPages());

        return "servicos/index";
    }

    @GetMapping("/create")
    public String createServico(Model model) {
        ServicoDTO servicoDTO = new ServicoDTO();
        model.addAttribute("servicoDTO", servicoDTO);

        return "servicos/create";
    }

    @PostMapping("/create")
    public String createServico(@Valid @ModelAttribute ServicoDTO servicoDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "servicos/create";
        }

        Servico servico = new Servico();
        servico.setNome(servicoDTO.getNome());
        servico.setDescricao(servicoDTO.getDescricao());
        servico.setPreco(servicoDTO.getPreco());
        servicoRepository.save(servico);

        return "redirect:/servicos/";
    }

    @GetMapping("/edit")
    public String editServico(@RequestParam Integer id, Model model) {
        Servico servico = servicoRepository.findById(id).orElse(null);
        if (servico == null) {
            return "redirect:/servicos";
        }

        ServicoDTO servicoDTO = new ServicoDTO();
        servicoDTO.setNome(servico.getNome());
        servicoDTO.setDescricao(servico.getDescricao());
        servicoDTO.setPreco(servico.getPreco());

        model.addAttribute("servico", servico);
        model.addAttribute("servicoDTO", servicoDTO);

        return "servicos/edit";
    }

    @PostMapping("/edit")
    public String editServico(
            Model model,
            @RequestParam Integer id,
            @Valid @ModelAttribute ServicoDTO servicoDTO,
            BindingResult bindingResult
    ) {
        Servico servico = servicoRepository.findById(id).orElse(null);
        if (servico == null) {
            return "redirect:/servicos";
        }

        model.addAttribute("servico", servico);

        if (bindingResult.hasErrors()) {
            model.addAttribute("servico", servico);
            return "servicos/edit";
        }

        servico.setNome(servicoDTO.getNome());
        servico.setDescricao(servicoDTO.getDescricao());
        servico.setPreco(servicoDTO.getPreco());
        servicoRepository.save(servico);

        return "redirect:/servicos";
    }

    @GetMapping("/delete")
    public String deleteServico(@RequestParam Integer id) {
        Servico servico = servicoRepository.findById(id).orElse(null);
        if (servico != null) {
            servicoRepository.delete(servico);
        }
        return "redirect:/servicos";
    }

}
