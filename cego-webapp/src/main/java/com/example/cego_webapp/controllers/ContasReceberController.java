package com.example.cego_webapp.controllers;

import com.example.cego_webapp.models.ContaReceber;
import com.example.cego_webapp.models.ContaReceberStatus;
import com.example.cego_webapp.models.FormaPagamento;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.ContaReceberRepository;
import com.example.cego_webapp.services.ContaReceberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/contas-a-receber")
public class ContasReceberController {

    @Autowired
    private ContaReceberService contaReceberService;

    @Autowired
    private ContaReceberRepository contaReceberRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping({"", "/"})
    public String listarContas(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer clienteId,
                               @RequestParam(required = false) LocalDate dataInicio,
                               @RequestParam(required = false) LocalDate dataFim,
                               @RequestParam(required = false) String origem,
                               @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size);
        ContaReceberStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try { statusEnum = ContaReceberStatus.valueOf(status); } catch (Exception e) {}
        }

        Page<ContaReceber> contasPage = contaReceberService.listarContas(keyword, statusEnum, clienteId, dataInicio, dataFim, origem, pageable);

        model.addAttribute("totalPendente", contaReceberRepository.findTotalPendente().orElse(BigDecimal.ZERO));
        model.addAttribute("totalAtrasado", contaReceberRepository.findTotalAtrasado().orElse(BigDecimal.ZERO));
        model.addAttribute("totalRecebidoMes", contaReceberRepository.findTotalRecebidoNoMes(LocalDate.now().withDayOfMonth(1)).orElse(BigDecimal.ZERO));

        model.addAttribute("contasPage", contasPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contasPage.getTotalPages());
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));

        model.addAttribute("paramKeyword", keyword);
        model.addAttribute("paramClienteId", clienteId);
        model.addAttribute("paramDataInicio", dataInicio);
        model.addAttribute("paramDataFim", dataFim);
        model.addAttribute("paramOrigem", origem);
        model.addAttribute("paramStatus", status);

        model.addAttribute("activePage", "contas-a-receber");

        return "contas-a-receber/index";
    }

    @PostMapping("/pagar")
    public String marcarComoPaga(@RequestParam Long id,
                                 @RequestParam FormaPagamento formaPagamento,
                                 RedirectAttributes redirectAttributes) {
        try {
            contaReceberService.marcarComoPaga(id, formaPagamento);
            redirectAttributes.addFlashAttribute("successMessage", "Conta #" + id + " marcada como recebida com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/contas-a-receber";
    }
}