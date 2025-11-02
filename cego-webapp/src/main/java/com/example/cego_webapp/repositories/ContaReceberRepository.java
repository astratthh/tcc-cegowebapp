package com.example.cego_webapp.repositories;

import com.example.cego_webapp.dto.FluxoCaixaDTO;
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
import java.util.List;
import java.util.Optional;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {

    // Método de busca principal, unificando filtros e ordenação customizada
    @Query("SELECT cr FROM ContaReceber cr " +
            "LEFT JOIN cr.venda v LEFT JOIN v.cliente vc " +
            "LEFT JOIN cr.ordemServico os LEFT JOIN os.cliente osc " +
            "WHERE " +
            "(:status IS NULL OR cr.status = :status) " +
            "AND (:clienteId IS NULL OR vc.id = :clienteId OR osc.id = :clienteId) " +
            "AND (CAST(:dataInicio AS date) IS NULL OR cr.dataVencimento >= :dataInicio) " +
            "AND (CAST(:dataFim AS date) IS NULL OR cr.dataVencimento <= :dataFim) " +
            "AND (:origem IS NULL OR :origem = '' OR " +
            "     (:origem = 'VENDA' AND cr.venda IS NOT NULL) OR " +
            "     (:origem = 'OS' AND cr.ordemServico IS NOT NULL)) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     LOWER(vc.nome) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(osc.nome) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY " +
            "  CASE cr.status " +
            "    WHEN 'PENDENTE' THEN 1 " +
            "    WHEN 'RECEBIDA' THEN 2 " +
            "    WHEN 'CANCELADA' THEN 3 " +
            "    ELSE 4 " +
            "  END, " +
            "cr.dataVencimento ASC")
    Page<ContaReceber> searchAndSort(@Param("keyword") String keyword,
                                     @Param("status") ContaReceberStatus status,
                                     @Param("clienteId") Integer clienteId,
                                     @Param("dataInicio") LocalDate dataInicio,
                                     @Param("dataFim") LocalDate dataFim,
                                     @Param("origem") String origem,
                                     Pageable pageable);

    // Método utilitário para o Service
    Optional<ContaReceber> findByOrdemServico(OrdemServico ordemServico);

    // --- Métodos para o Dashboard ---
    @Query("SELECT SUM(c.valor) FROM ContaReceber c WHERE c.status = 'PENDENTE'")
    Optional<BigDecimal> findTotalPendente();

    @Query("SELECT SUM(c.valor) FROM ContaReceber c WHERE c.status = 'PENDENTE' AND c.dataVencimento < CURRENT_DATE")
    Optional<BigDecimal> findTotalAtrasado();

    @Query("SELECT SUM(c.valor) FROM ContaReceber c WHERE c.status = 'RECEBIDA' AND c.dataRecebimento >= :inicioMes")
    Optional<BigDecimal> findTotalRecebidoNoMes(@Param("inicioMes") LocalDate inicioMes);

    // Retorna todas as contas recebidas que vieram de VENDAS em um período
    @Query("SELECT cr FROM ContaReceber cr JOIN FETCH cr.venda v JOIN FETCH v.cliente WHERE cr.venda IS NOT NULL AND cr.status = 'RECEBIDA' AND cr.dataRecebimento BETWEEN :inicio AND :fim ORDER BY cr.dataRecebimento")
    List<ContaReceber> findVendasRecebidasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Retorna todas as contas recebidas que vieram de OS em um período
    @Query("SELECT cr FROM ContaReceber cr JOIN FETCH cr.ordemServico os JOIN FETCH os.cliente WHERE cr.ordemServico IS NOT NULL AND cr.status = 'RECEBIDA' AND cr.dataRecebimento BETWEEN :inicio AND :fim ORDER BY cr.dataRecebimento")
    List<ContaReceber> findOsRecebidasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT new com.example.cego_webapp.dto.FluxoCaixaDTO(cr.formaPagamento, SUM(cr.valor)) " +
            "FROM ContaReceber cr " +
            "WHERE cr.status = 'RECEBIDA' " +
            "AND cr.dataRecebimento BETWEEN :inicio AND :fim " +
            "GROUP BY cr.formaPagamento")
    List<FluxoCaixaDTO> sumRecebidoGroupByFormaPagamento(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}