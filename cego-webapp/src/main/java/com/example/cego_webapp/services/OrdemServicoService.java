// src/main/java/com/example/cego_webapp/services/OrdemServicoService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.OrdemServicoDTO;
import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class OrdemServicoService {

    @Autowired private OrdemServicoRepository osRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private ContaReceberRepository contaReceberRepository;

    public Page<OrdemServico> listarOrdensServico(Integer clienteId, Integer funcionarioId, StatusOrdemServico status,
                                                  LocalDate dataInicio, LocalDate dataFim, Integer servicoId, Pageable pageable) {
        return osRepository.search(clienteId, funcionarioId, status, dataInicio, dataFim, servicoId, pageable);
    }

    public OrdemServico buscarPorId(Integer id) {
        return osRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ordem de Serviço #" + id + " não encontrada."));
    }

    @Transactional
    public OrdemServico criarOrdemServico(OrdemServicoDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        Veiculo veiculo = veiculoRepository.findById(dto.getVeiculoId()).orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado."));
        if (!veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalStateException("O veículo selecionado não pertence ao cliente.");
        }

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
            os.setFuncionarios(new HashSet<>(funcionarioRepository.findAllById(dto.getFuncionarioIds())));
        }
        os.recalcularValorTotal();
        return osRepository.save(os);
    }

    @Transactional
    public OrdemServico atualizarOrdemServico(Integer id, OrdemServicoDTO dto) {
        OrdemServico os = buscarPorId(id);
        if (os.getStatus() == StatusOrdemServico.FINALIZADA || os.getStatus() == StatusOrdemServico.CANCELADA) {
            throw new IllegalStateException("OS não pode ser atualizada pois está finalizada ou cancelada.");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        Veiculo veiculo = veiculoRepository.findById(dto.getVeiculoId()).orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado."));
        if (!veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalStateException("O veículo selecionado não pertence ao cliente.");
        }

        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setObservacoes(dto.getObservacoes());
        os.setPrevisaoEntrega(dto.getPrevisaoEntrega());

        os.getItens().clear();
        List<Servico> servicos = servicoRepository.findAllById(dto.getServicoIds());
        servicos.forEach(s -> os.getItens().add(new ItemServico(os, s, 1)));

        os.getFuncionarios().clear();
        if (dto.getFuncionarioIds() != null && !dto.getFuncionarioIds().isEmpty()) {
            os.setFuncionarios(new HashSet<>(funcionarioRepository.findAllById(dto.getFuncionarioIds())));
        }
        os.recalcularValorTotal();
        os.setObservacoesInternas(os.getObservacoesInternas() + "\nOS editada em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".");

        return osRepository.save(os);
    }

    @Transactional
    public void alterarStatus(Integer id, String novoStatusStr) {
        OrdemServico os = buscarPorId(id);
        StatusOrdemServico novoStatus = StatusOrdemServico.valueOf(novoStatusStr);

        os.setStatus(novoStatus);
        os.setObservacoesInternas(os.getObservacoesInternas() + "\nStatus alterado para '" + novoStatus.getDescricao() + "' em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".");

        if (novoStatus == StatusOrdemServico.FINALIZADA) {
            os.setDataFinalizacao(LocalDateTime.now());
            ContaReceber conta = new ContaReceber();
            conta.setValor(os.getValorTotal());
            conta.setDataVencimento(LocalDate.now().plusDays(30));
            conta.setStatus(ContaReceberStatus.PENDENTE);
            conta.setOrdemServico(os);
            contaReceberRepository.save(conta);
        }
        osRepository.save(os);
    }

    @Transactional
    public void cancelarOS(Integer id) {
        OrdemServico os = buscarPorId(id);
        Optional<ContaReceber> contaOpt = contaReceberRepository.findByOrdemServico(os);
        if (contaOpt.isPresent() && contaOpt.get().getStatus() == ContaReceberStatus.RECEBIDA) {
            throw new IllegalStateException("Não é possível cancelar a OS #" + id + ", pois o pagamento já foi recebido.");
        }

        contaOpt.ifPresent(conta -> {
            conta.setStatus(ContaReceberStatus.CANCELADA);
            contaReceberRepository.save(conta);
        });

        os.setStatus(StatusOrdemServico.CANCELADA);
        os.setObservacoesInternas(os.getObservacoesInternas() + "\nOS Cancelada em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".");
        osRepository.save(os);
    }
}