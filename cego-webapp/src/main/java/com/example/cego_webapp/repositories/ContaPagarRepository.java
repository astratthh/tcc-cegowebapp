package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.ContaPagar;
import com.example.cego_webapp.models.ContaPagarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long> {

    @Query("SELECT c FROM ContaPagar c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:fornecedorId IS NULL OR c.compra.fornecedor.id = :fornecedorId) AND " +
            "(:dataInicio IS NULL OR c.dataVencimento >= :dataInicio) AND " +
            "(:dataFim IS NULL OR c.dataVencimento <= :dataFim)")
    Page<ContaPagar> findWithFilters(
            @Param("status") ContaPagarStatus status,
            @Param("fornecedorId") Integer fornecedorId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable
    );

    @Query("SELECT SUM(c.valor) FROM ContaPagar c WHERE c.status = 'A_PAGAR'")
    Optional<BigDecimal> findTotalAPagar();

    @Query("SELECT SUM(c.valor) FROM ContaPagar c WHERE c.status = 'A_PAGAR' AND c.dataVencimento < CURRENT_DATE")
    Optional<BigDecimal> findTotalVencido();

    @Query("SELECT SUM(c.valor) FROM ContaPagar c WHERE c.status = 'PAGA' AND c.dataPagamento >= :inicioMes")
    Optional<BigDecimal> findTotalPagoNoMes(@Param("inicioMes") LocalDate inicioMes);
}