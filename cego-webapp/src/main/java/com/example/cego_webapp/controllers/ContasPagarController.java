package com.example.cego_webapp.controllers;

import com.example.cego_webapp.models.ContaPagar;
import com.example.cego_webapp.models.ContaPagarStatus;
import com.example.cego_webapp.repositories.ContaPagarRepository;
import com.example.cego_webapp.repositories.FornecedorRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/contas-a-pagar")
public class ContasPagarController {

    @Autowired private ContaPagarRepository contaPagarRepository;
    @Autowired private FornecedorRepository fornecedorRepository;

    @GetMapping({"", "/"})
    public String index(Model model,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Integer fornecedorId,
                        @RequestParam(required = false) LocalDate dataInicio,
                        @RequestParam(required = false) LocalDate dataFim,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dataVencimento"));
        ContaPagarStatus statusEnum = null;
        if(status != null && !status.isEmpty()){
            try {
                statusEnum = ContaPagarStatus.valueOf(status);
            } catch (IllegalArgumentException e) { /* Ignora status inválido */ }
        }

        Page<ContaPagar> contasPage = contaPagarRepository.findWithFilters(statusEnum, fornecedorId, dataInicio, dataFim, pageable);

        model.addAttribute("contasPage", contasPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contasPage.getTotalPages());

        // Adiciona totais para o dashboard
        model.addAttribute("totalAPagar", contaPagarRepository.findTotalAPagar().orElse(BigDecimal.ZERO));
        model.addAttribute("totalVencido", contaPagarRepository.findTotalVencido().orElse(BigDecimal.ZERO));
        model.addAttribute("totalPagoMes", contaPagarRepository.findTotalPagoNoMes(LocalDate.now().withDayOfMonth(1)).orElse(BigDecimal.ZERO));

        // Adiciona dados para os filtros
        model.addAttribute("fornecedores", fornecedorRepository.findAll(Sort.by("nome")));

        // ### MELHORIA ADICIONADA AQUI ###
        // Devolve os parâmetros para manter os filtros preenchidos na tela de forma explícita
        model.addAttribute("paramStatus", status);
        model.addAttribute("paramFornecedorId", fornecedorId);
        model.addAttribute("paramDataInicio", dataInicio);
        model.addAttribute("paramDataFim", dataFim);


        return "contas-a-pagar/index";
    }

    @GetMapping("/confirmar-pagamento")
    public String confirmarPagamento(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        contaPagarRepository.findById(id).ifPresent(conta -> {
            if (conta.getStatus() == ContaPagarStatus.A_PAGAR) {
                conta.setStatus(ContaPagarStatus.PAGA);
                conta.setDataPagamento(LocalDate.now());
                contaPagarRepository.save(conta);
                redirectAttributes.addFlashAttribute("successMessage", "Conta da Compra #" + conta.getCompra().getId() + " marcada como PAGA!");
            }
        });
        return "redirect:/contas-a-pagar";
    }
}