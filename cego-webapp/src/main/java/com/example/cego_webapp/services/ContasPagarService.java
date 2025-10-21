// src/main/java/com/example/cego_webapp/services/ContasPagarService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.models.ContaPagar;
import com.example.cego_webapp.models.ContaPagarStatus;
import com.example.cego_webapp.models.FormaPagamento;
import com.example.cego_webapp.repositories.ContaPagarRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContasPagarService {

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    public Page<ContaPagar> listarContas(ContaPagarStatus status, Integer fornecedorId,
                                         LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        return contaPagarRepository.searchAndSort(status, fornecedorId, dataInicio, dataFim, pageable);
    }

    public Map<String, BigDecimal> getDashboardInfo() {
        Map<String, BigDecimal> info = new HashMap<>();
        info.put("totalAPagar", contaPagarRepository.findTotalAPagar().orElse(BigDecimal.ZERO));
        info.put("totalVencido", contaPagarRepository.findTotalVencido().orElse(BigDecimal.ZERO));
        info.put("totalPagoMes", contaPagarRepository.findTotalPagoNoMes(LocalDate.now().withDayOfMonth(1)).orElse(BigDecimal.ZERO));
        return info;
    }

    @Transactional
    public void marcarComoPaga(Long id, FormaPagamento formaPagamento) { // Parâmetro adicionado
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta a Pagar com ID " + id + " não encontrada."));

        if (conta.getStatus() != ContaPagarStatus.A_PAGAR) {
            throw new IllegalStateException("Apenas contas com status 'A Pagar' podem ser pagas.");
        }
        if (formaPagamento == null) { // Validação básica
            throw new IllegalArgumentException("Forma de pagamento é obrigatória.");
        }

        conta.setStatus(ContaPagarStatus.PAGA);
        conta.setDataPagamento(LocalDate.now());
        conta.setFormaPagamento(formaPagamento); // Define a forma de pagamento
        contaPagarRepository.save(conta);
    }
}