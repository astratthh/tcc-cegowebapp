package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.ContaReceber;
import com.example.cego_webapp.models.ContaReceberStatus;
import com.example.cego_webapp.models.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {

    @Query("SELECT cr FROM ContaReceber cr " +
            "LEFT JOIN cr.venda v LEFT JOIN v.cliente vc " +
            "LEFT JOIN cr.ordemServico os LEFT JOIN os.cliente osc " +
            "WHERE " +
            "(:status IS NULL OR cr.status = :status) AND " +
            "(:dataInicio IS NULL OR cr.dataVencimento >= :dataInicio) AND " +
            "(:dataFim IS NULL OR cr.dataVencimento <= :dataFim) AND " +
            "(:clienteId IS NULL OR vc.id = :clienteId OR osc.id = :clienteId) AND " +
            // ### CORREÇÃO APLICADA AQUI ###
            "(:origem IS NULL OR :origem = '' OR " + // Adicionada a verificação de string vazia
            " (:origem = 'VENDA' AND cr.venda IS NOT NULL) OR " +
            " (:origem = 'OS' AND cr.ordemServico IS NOT NULL))")
    Page<ContaReceber> search(@Param("status") ContaReceberStatus status,
                              @Param("dataInicio") LocalDate dataInicio,
                              @Param("dataFim") LocalDate dataFim,
                              @Param("clienteId") Integer clienteId,
                              @Param("origem") String origem,
                              Pageable pageable);

    @Query("SELECT c FROM ContaReceber c WHERE c.ordemServico = :ordemServico")
    Optional<ContaReceber> findByOrdemServico(@Param("ordemServico") OrdemServico ordemServico);

    // Métodos para o dashboard (permanecem os mesmos)
    @Query("SELECT SUM(c.valor) FROM ContaReceber c WHERE c.status = 'PENDENTE'")
    BigDecimal findTotalPendente();

    @Query("SELECT SUM(c.valor) FROM ContaReceber c WHERE c.status = 'PENDENTE' AND c.dataVencimento < CURRENT_DATE")
    BigDecimal findTotalAtrasado();

    @Query("SELECT SUM(c.valor) FROM ContaReceber c WHERE c.status = 'RECEBIDA' AND c.dataRecebimento >= :inicioMes")
    BigDecimal findTotalRecebidoNoMes(@Param("inicioMes") LocalDate inicioMes);
}