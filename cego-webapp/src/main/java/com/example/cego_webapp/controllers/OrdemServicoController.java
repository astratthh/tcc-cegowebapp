package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.OrdemServicoDTO;
import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    @Autowired private OrdemServicoRepository ordemServicoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private ContaReceberRepository contaReceberRepository;

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

        Page<OrdemServico> osPage = ordemServicoRepository.search(clienteId, funcionarioId, statusEnum, dataInicio, dataFim, servicoId, pageable);

        model.addAttribute("osPage", osPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", osPage.getTotalPages());

        // Carrega dados para os dropdowns de filtro
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
        model.addAttribute("funcionarios", funcionarioRepository.findAll(Sort.by("nome")));
        model.addAttribute("servicos", servicoRepository.findAll(Sort.by("nome")));

        // Devolve os parâmetros para manter os filtros preenchidos na tela
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
    @Transactional
    public String createOrdemServico(@ModelAttribute("dto") OrdemServicoDTO dto, RedirectAttributes redirectAttributes) {
        try {
            if (dto.getClienteId() == null || dto.getVeiculoId() == null || dto.getServicoIds() == null || dto.getServicoIds().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cliente, Veículo e ao menos um Serviço são obrigatórios.");
                return "redirect:/ordens-servico/create?clienteId=" + dto.getClienteId();
            }
            Cliente cliente = clienteRepository.findById(dto.getClienteId()).orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));
            Veiculo veiculo = veiculoRepository.findById(dto.getVeiculoId()).orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado."));

            OrdemServico os = new OrdemServico();
            os.setCliente(cliente);
            os.setVeiculo(veiculo);
            os.setObservacoes(dto.getObservacoes());
            os.setPrevisaoEntrega(dto.getPrevisaoEntrega());
            os.setStatus(StatusOrdemServico.PENDENTE);
            os.setObservacoesInternas("OS Criada em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".");

            List<Servico> servicos = servicoRepository.findAllById(dto.getServicoIds());
            for (Servico s : servicos) {
                os.getItens().add(new ItemServico(os, s, 1));
            }
            if (dto.getFuncionarioIds() != null && !dto.getFuncionarioIds().isEmpty()) {
                List<Funcionario> funcionarios = funcionarioRepository.findAllById(dto.getFuncionarioIds());
                os.setFuncionarios(new HashSet<>(funcionarios));
            }
            os.recalcularValorTotal();
            ordemServicoRepository.save(os);

            redirectAttributes.addFlashAttribute("successMessage", "Ordem de Serviço #" + os.getId() + " aberta com sucesso!");
            return "redirect:/ordens-servico";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/ordens-servico/create?clienteId=" + dto.getClienteId();
        }
    }

    @GetMapping("/details/{id}")
    public String showDetails(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            OrdemServico os = ordemServicoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço #" + id + " não encontrada."));
            model.addAttribute("os", os);
            return "ordens-servico/details";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/ordens-servico";
        }
    }

    @PostMapping("/alterar-status/{id}")
    @Transactional
    public String alterarStatus(@PathVariable Integer id, @RequestParam String novoStatus, RedirectAttributes redirectAttributes) {
        OrdemServico os = ordemServicoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("OS não encontrada"));
        StatusOrdemServico status = StatusOrdemServico.valueOf(novoStatus);
        String log = "\nStatus alterado para '" + status.getDescricao() + "' em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".";
        os.setStatus(status);
        os.setObservacoesInternas(os.getObservacoesInternas() + log);

        if (status == StatusOrdemServico.FINALIZADA) {
            os.setDataFinalizacao(LocalDateTime.now());
            ContaReceber conta = new ContaReceber();
            conta.setValor(os.getValorTotal());
            conta.setDataVencimento(LocalDate.now().plusDays(30));
            conta.setStatus(ContaReceberStatus.PENDENTE);
            conta.setOrdemServico(os);
            contaReceberRepository.save(conta);
        }
        ordemServicoRepository.save(os);
        redirectAttributes.addFlashAttribute("successMessage", "Status da OS #" + os.getId() + " alterado para " + status.getDescricao() + "!");
        return "redirect:/ordens-servico";
    }

    @PostMapping("/cancelar/{id}")
    @Transactional
    public String cancelarOS(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        OrdemServico os = ordemServicoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("OS não encontrada"));
        Optional<ContaReceber> contaOpt = contaReceberRepository.findByOrdemServico(os);
        if (contaOpt.isPresent() && contaOpt.get().getStatus() == ContaReceberStatus.RECEBIDA) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível cancelar a OS #" + id + ", pois o pagamento já foi recebido.");
            return "redirect:/ordens-servico";
        }
        if (contaOpt.isPresent() && contaOpt.get().getStatus() == ContaReceberStatus.PENDENTE) {
            ContaReceber conta = contaOpt.get();
            conta.setStatus(ContaReceberStatus.CANCELADA);
            contaReceberRepository.save(conta);
        }
        os.setStatus(StatusOrdemServico.CANCELADA);
        String log = "\nOS Cancelada em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".";
        os.setObservacoesInternas(os.getObservacoesInternas() + log);
        ordemServicoRepository.save(os);
        redirectAttributes.addFlashAttribute("successMessage", "Ordem de Serviço #" + id + " foi cancelada.");
        return "redirect:/ordens-servico";
    }
}