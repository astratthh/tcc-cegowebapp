package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.OrdemServicoDTO;
import com.example.cego_webapp.models.OrdemServico;
import com.example.cego_webapp.models.StatusOrdemServico;
import com.example.cego_webapp.models.Veiculo;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.FuncionarioRepository;
import com.example.cego_webapp.repositories.ServicoRepository;
import com.example.cego_webapp.repositories.VeiculoRepository;
import com.example.cego_webapp.services.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService osService;

    // Repositórios injetados apenas para popular os formulários com opções
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;

    @GetMapping({"", "/"})
    public String listOrdens(Model model,
                             @RequestParam(required = false) Integer clienteId,
                             @RequestParam(required = false) Integer funcionarioId,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) LocalDate dataInicio,
                             @RequestParam(required = false) LocalDate dataFim,
                             @RequestParam(required = false) Integer servicoId,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataEntrada"));
        StatusOrdemServico statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try { statusEnum = StatusOrdemServico.valueOf(status); } catch (Exception e) { /* Ignora status inválido */ }
        }

        Page<OrdemServico> osPage = osService.listarOrdensServico(clienteId, funcionarioId, statusEnum, dataInicio, dataFim, servicoId, pageable);

        model.addAttribute("osPage", osPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", osPage.getTotalPages());
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
        model.addAttribute("funcionarios", funcionarioRepository.findAll(Sort.by("nome")));
        model.addAttribute("servicos", servicoRepository.findAll(Sort.by("nome")));
        model.addAttribute("paramClienteId", clienteId);
        model.addAttribute("paramFuncionarioId", funcionarioId);
        model.addAttribute("paramStatus", status);
        model.addAttribute("paramDataInicio", dataInicio);
        model.addAttribute("paramDataFim", dataFim);
        model.addAttribute("paramServicoId", servicoId);

        return "ordens-servico/index";
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(name = "clienteId", required = false) Integer clienteId, Model model) {
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
        model.addAttribute("servicos", servicoRepository.findAll(Sort.by("nome")));
        model.addAttribute("funcionarios", funcionarioRepository.findAll(Sort.by("nome")));

        OrdemServicoDTO dto = new OrdemServicoDTO();
        List<Veiculo> veiculosDoCliente = new ArrayList<>();

        if (clienteId != null) {
            veiculosDoCliente = veiculoRepository.findByClienteId(clienteId);
            dto.setClienteId(clienteId);
        }

        model.addAttribute("dto", dto);
        model.addAttribute("veiculos", veiculosDoCliente);

        return "ordens-servico/create";
    }

    @PostMapping("/create")
    public String createOrdemServico(@ModelAttribute("dto") OrdemServicoDTO dto, RedirectAttributes redirectAttributes) {
        try {
            OrdemServico os = osService.criarOrdemServico(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Ordem de Serviço #" + os.getId() + " aberta com sucesso!");
            return "redirect:/ordens-servico";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar OS: " + e.getMessage());
            return "redirect:/ordens-servico/create?clienteId=" + dto.getClienteId();
        }
    }

    @GetMapping("/details/{id}")
    public String showDetails(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            OrdemServico os = osService.buscarPorId(id);
            model.addAttribute("os", os);
            return "ordens-servico/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/ordens-servico";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            OrdemServico os = osService.buscarPorId(id);
            if (os.getStatus() == StatusOrdemServico.FINALIZADA || os.getStatus() == StatusOrdemServico.CANCELADA) {
                redirectAttributes.addFlashAttribute("errorMessage", "Esta OS não pode ser editada.");
                return "redirect:/ordens-servico";
            }

            // 1. Envia o DTO preenchido com os dados da OS
            model.addAttribute("dto", new OrdemServicoDTO(os));
            // 2. Envia o objeto OS para usar o ID no título
            model.addAttribute("os", os);
            // 3. Envia as listas para o Thymeleaf montar os menus <select>
            model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
            model.addAttribute("veiculosDoCliente", veiculoRepository.findByClienteId(os.getCliente().getId()));
            model.addAttribute("servicos", servicoRepository.findAll(Sort.by("nome")));
            model.addAttribute("funcionarios", funcionarioRepository.findAll(Sort.by("nome")));

            return "ordens-servico/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/ordens-servico";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateOrdemServico(@PathVariable Integer id, @ModelAttribute("dto") OrdemServicoDTO dto, RedirectAttributes redirectAttributes) {
        try {
            osService.atualizarOrdemServico(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Ordem de Serviço #" + id + " atualizada com sucesso!");
            return "redirect:/ordens-servico/details/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar a OS: " + e.getMessage());
            // Devolve o DTO para o formulário não perder os dados em caso de erro
            redirectAttributes.addFlashAttribute("dto", dto);
            return "redirect:/ordens-servico/edit/" + id;
        }
    }

    @PostMapping("/alterar-status/{id}")
    public String alterarStatus(@PathVariable Integer id, @RequestParam String novoStatus, RedirectAttributes redirectAttributes) {
        try {
            osService.alterarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Status da OS #" + id + " alterado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ordens-servico";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarOS(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            osService.cancelarOS(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ordem de Serviço #" + id + " foi cancelada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/ordens-servico";
    }
}