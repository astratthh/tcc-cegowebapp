package com.example.cego_webapp.controllers;

import com.example.cego_webapp.models.ContaReceber;
import com.example.cego_webapp.models.ContaReceberStatus;
import com.example.cego_webapp.models.VendaStatus;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.ContaReceberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/contas-a-receber")
public class ContasReceberController {

    @Autowired private ContaReceberRepository contaReceberRepository;
    @Autowired private ClienteRepository clienteRepository;

    @GetMapping({"", "/"})
    public String index(Model model,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) LocalDate dataInicio,
                        @RequestParam(required = false) LocalDate dataFim,
                        @RequestParam(required = false) Integer clienteId,
                        @RequestParam(required = false) String origem,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dataVencimento"));

        ContaReceberStatus statusEnum = null;
        LocalDate dataFimAjustada = dataFim;

        // Lógica especial para o filtro "Atrasado"
        if ("ATRASADO".equals(status)) {
            statusEnum = ContaReceberStatus.PENDENTE;
            // Define a data final como "ontem" para buscar apenas os vencidos
            dataFimAjustada = LocalDate.now().minusDays(1);
        } else if (status != null && !status.isEmpty()) {
            try {
                statusEnum = ContaReceberStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Ignora parâmetro de status inválido
            }
        }

        // Chama o método de busca robusto do repositório
        Page<ContaReceber> contasPage = contaReceberRepository.search(statusEnum, dataInicio, dataFimAjustada, clienteId, origem, pageable);

        // Envia dados para o dashboard (totais gerais)
        model.addAttribute("totalPendente", contaReceberRepository.findTotalPendente());
        model.addAttribute("totalAtrasado", contaReceberRepository.findTotalAtrasado());
        model.addAttribute("totalRecebidoMes", contaReceberRepository.findTotalRecebidoNoMes(LocalDate.now().withDayOfMonth(1)));

        // Envia dados da paginação e filtros para a view
        model.addAttribute("contasPage", contasPage);
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contasPage.getTotalPages());

        // Devolve os parâmetros originais para manter os filtros preenchidos na tela
        model.addAttribute("paramStatus", status);
        model.addAttribute("paramDataInicio", dataInicio);
        model.addAttribute("paramDataFim", dataFim);
        model.addAttribute("paramClienteId", clienteId);
        model.addAttribute("paramOrigem", origem);

        return "contas-a-receber/index";
    }

    @GetMapping("/confirmar-pagamento")
    public String confirmarPagamento(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        contaReceberRepository.findById(id).ifPresent(conta -> {
            if (conta.getStatus() == ContaReceberStatus.PENDENTE) {
                conta.setStatus(ContaReceberStatus.RECEBIDA);
                conta.setDataRecebimento(LocalDate.now());

                if (conta.getVenda() != null && conta.getVenda().getStatus() != null) {
                    conta.getVenda().setStatus(VendaStatus.PAGA);
                }

                contaReceberRepository.save(conta);
                redirectAttributes.addFlashAttribute("successMessage", "Pagamento confirmado com sucesso!");
            }
        });
        return "redirect:/contas-a-receber";
    }
}