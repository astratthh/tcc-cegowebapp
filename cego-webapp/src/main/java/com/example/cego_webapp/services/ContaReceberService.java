package com.example.cego_webapp.services;

import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.ContaReceberRepository;
import com.example.cego_webapp.repositories.OrdemServicoRepository;
import com.example.cego_webapp.repositories.VendaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ContaReceberService {

    @Autowired
    private OrdemServicoRepository osRepository;
    @Autowired
    private ContaReceberRepository contaReceberRepository;
    @Autowired
    private VendaRepository vendaRepository;

    /**
     * Lista e filtra as contas a receber usando a query customizada
     * que também aplica a ordenação de "Pendentes primeiro".
     */
    public Page<ContaReceber> listarContas(String keyword, ContaReceberStatus status, Integer clienteId,
                                           LocalDate dataInicio, LocalDate dataFim, String origem,
                                           Pageable pageable) {
        // Chama o novo método unificado do repositório
        return contaReceberRepository.searchAndSort(keyword, status, clienteId, dataInicio, dataFim, origem, pageable);
    }

    /**
     * Busca uma única conta a receber pelo seu ID.
     * Lança uma exceção se não for encontrada.
     */
    public ContaReceber buscarPorId(Long id) {
        return contaReceberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Conta a Receber com ID " + id + " não encontrada."));
    }

    /**
     * Altera o status de uma conta para "RECEBIDA" e define a data de recebimento.
     * A operação é transacional.
     */
    @Transactional
    public void marcarComoPaga(Long id, FormaPagamento formaPagamento) {
        ContaReceber conta = buscarPorId(id);
        if (conta.getStatus() != ContaReceberStatus.PENDENTE) {
            throw new IllegalStateException("Apenas contas com status PENDENTE podem ser marcadas como pagas.");
        }
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória.");
        }

        // 1. Atualiza a Conta a Receber
        conta.setStatus(ContaReceberStatus.RECEBIDA);
        conta.setDataRecebimento(LocalDate.now());
        conta.setFormaPagamento(formaPagamento);

        // ### LÓGICA DE SINCRONIZAÇÃO ADICIONADA ###

        // 2. Verifica se a conta veio de uma Venda
        if (conta.getVenda() != null) {
            Venda venda = conta.getVenda();
            venda.setStatus(VendaStatus.PAGA); // Atualiza o status da Venda
            vendaRepository.save(venda); // Salva a Venda
        }

        // 3. Verifica se a conta veio de uma Ordem de Serviço
        if (conta.getOrdemServico() != null) {
            OrdemServico os = conta.getOrdemServico();
            os.setStatus(StatusOrdemServico.PAGA); // Atualiza o status da OS
            osRepository.save(os); // Salva a OS
        }

        contaReceberRepository.save(conta); // Salva a Conta a Receber
    }
}