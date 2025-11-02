package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.OrdemServico;
import com.example.cego_webapp.models.StatusOrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer> {

    @Query("SELECT os FROM OrdemServico os WHERE " +
            "(:clienteId IS NULL OR os.cliente.id = :clienteId) AND " +
            "(:funcionarioId IS NULL OR EXISTS (SELECT f FROM os.funcionarios f WHERE f.id = :funcionarioId)) AND " +
            "(:status IS NULL OR os.status = :status) AND " +
            "(CAST(:dataInicio AS date) IS NULL OR os.dataEntrada >= :dataInicio) AND " +
            "(CAST(:dataFim AS date) IS NULL OR os.dataEntrada <= :dataFim) AND " +
            "(:servicoId IS NULL OR EXISTS (SELECT i FROM ItemServico i WHERE i.ordemServico = os AND i.servico.id = :servicoId))")
    Page<OrdemServico> search(@Param("clienteId") Integer clienteId,
                              @Param("funcionarioId") Integer funcionarioId,
                              @Param("status") StatusOrdemServico status,
                              @Param("dataInicio") LocalDate dataInicio,
                              @Param("dataFim") LocalDate dataFim,
                              @Param("servicoId") Integer servicoId,
                              Pageable pageable);
}