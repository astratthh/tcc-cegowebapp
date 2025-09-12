package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    @Override
    @EntityGraph(attributePaths = {"fornecedor", "itens", "itens.produto", "contaPagar"})
    Page<Compra> findAll(Pageable pageable);

    // NOVO: Calcula o custo total das compras finalizadas no mês atual
    @Query("SELECT SUM(c.valorTotal) FROM Compra c WHERE c.status = 'FINALIZADA' AND EXTRACT(MONTH FROM c.dataCompra) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM c.dataCompra) = EXTRACT(YEAR FROM CURRENT_DATE)")
    Optional<BigDecimal> findCustoMesAtual();

    // NOVO: Calcula o custo médio de todas as compras finalizadas
    @Query("SELECT AVG(c.valorTotal) FROM Compra c WHERE c.status = 'FINALIZADA'")
    Optional<BigDecimal> findCustoMedio();
}