// src/main/java/com/example/cego_webapp/controllers/ContasPagarController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.models.ContaPagar;
import com.example.cego_webapp.models.ContaPagarStatus;
import com.example.cego_webapp.models.FormaPagamento;
import com.example.cego_webapp.repositories.FornecedorRepository;
import com.example.cego_webapp.services.ContasPagarService;
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
import java.util.Map;

@Controller
@RequestMapping("/contas-a-pagar")
public class ContasPagarController {

    @Autowired private ContasPagarService contasPagarService;
    @Autowired private FornecedorRepository fornecedorRepository;

    @GetMapping({"", "/"})
    public String index(Model model,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Integer fornecedorId,
                        @RequestParam(required = false) LocalDate dataInicio,
                        @RequestParam(required = false) LocalDate dataFim,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size); // A ordenação agora é feita no repositório
        ContaPagarStatus statusEnum = null;
        if(status != null && !status.isEmpty()){
            try { statusEnum = ContaPagarStatus.valueOf(status); } catch (Exception e) {}
        }

        Page<ContaPagar> contasPage = contasPagarService.listarContas(statusEnum, fornecedorId, dataInicio, dataFim, pageable);
        Map<String, BigDecimal> dashboardInfo = contasPagarService.getDashboardInfo();

        model.addAttribute("contasPage", contasPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contasPage.getTotalPages());
        model.addAttribute("totalAPagar", dashboardInfo.get("totalAPagar"));
        model.addAttribute("totalVencido", dashboardInfo.get("totalVencido"));
        model.addAttribute("totalPagoMes", dashboardInfo.get("totalPagoMes"));
        model.addAttribute("fornecedores", fornecedorRepository.findAll(Sort.by("nome")));

        // Devolve os parâmetros para manter os filtros preenchidos
        model.addAttribute("paramStatus", status);
        model.addAttribute("paramFornecedorId", fornecedorId);
        model.addAttribute("paramDataInicio", dataInicio);
        model.addAttribute("paramDataFim", dataFim);

        model.addAttribute("activePage", "contas-a-pagar");

        return "contas-a-pagar/index";
    }

    // ### CORREÇÃO: MUDADO DE GET PARA POST ###
    @PostMapping("/pagar")
    public String marcarComoPaga(@RequestParam Long id,
                                 @RequestParam FormaPagamento formaPagamento, // Parâmetro recebido
                                 RedirectAttributes redirectAttributes) {
        try {
            contasPagarService.marcarComoPaga(id, formaPagamento); // Passado para o service
            redirectAttributes.addFlashAttribute("successMessage", "Conta #" + id + " marcada como PAGA!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/contas-a-pagar";
    }
}