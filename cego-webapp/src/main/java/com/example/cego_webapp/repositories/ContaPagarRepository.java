package com.example.cego_webapp.repositories;

import com.example.cego_webapp.dto.FluxoCaixaDTO;
import com.example.cego_webapp.models.ContaPagar;
import com.example.cego_webapp.models.ContaPagarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long> {

    /**
     * Método principal de busca que unifica filtros e a ordenação customizada.
     * Ordena por status (A_PAGAR primeiro) e depois por data de vencimento.
     */
    @Query("SELECT cp FROM ContaPagar cp JOIN cp.compra c JOIN c.fornecedor f " +
            "WHERE (:status IS NULL OR cp.status = :status) " +
            "AND (:fornecedorId IS NULL OR f.id = :fornecedorId) " +
            "AND (CAST(:dataInicio AS date) IS NULL OR cp.dataVencimento >= :dataInicio) " +
            "AND (CAST(:dataFim AS date) IS NULL OR cp.dataVencimento <= :dataFim) " +
            "ORDER BY " +
            "  CASE cp.status " +
            "    WHEN 'A_PAGAR' THEN 1 " +
            "    WHEN 'PAGA' THEN 2 " +
            "    WHEN 'CANCELADA' THEN 3 " +
            "    ELSE 4 " +
            "  END, " +
            "cp.dataVencimento ASC")
    Page<ContaPagar> searchAndSort(@Param("status") ContaPagarStatus status,
                                   @Param("fornecedorId") Integer fornecedorId,
                                   @Param("dataInicio") LocalDate dataInicio,
                                   @Param("dataFim") LocalDate dataFim,
                                   Pageable pageable);

    // --- Métodos para o Dashboard (já estavam corretos) ---
    @Query("SELECT SUM(c.valor) FROM ContaPagar c WHERE c.status = 'A_PAGAR'")
    Optional<BigDecimal> findTotalAPagar();

    @Query("SELECT SUM(c.valor) FROM ContaPagar c WHERE c.status = 'A_PAGAR' AND c.dataVencimento < CURRENT_DATE")
    Optional<BigDecimal> findTotalVencido();

    @Query("SELECT SUM(c.valor) FROM ContaPagar c WHERE c.status = 'PAGA' AND c.dataPagamento >= :inicioMes")
    Optional<BigDecimal> findTotalPagoNoMes(@Param("inicioMes") LocalDate inicioMes);

    @Query("SELECT cp FROM ContaPagar cp JOIN FETCH cp.compra c JOIN FETCH c.fornecedor " +
            "WHERE cp.status = 'PAGA' AND cp.dataPagamento BETWEEN :inicio AND :fim " +
            "ORDER BY cp.dataPagamento ASC")
    List<ContaPagar> findPagasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}