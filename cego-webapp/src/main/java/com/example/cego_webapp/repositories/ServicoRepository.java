package com.example.cego_webapp.repositories;

import com.example.cego_webapp.dto.ServicoDesempenhoDTO;
import com.example.cego_webapp.models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    Page<Servico> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);

    // Retorna uma LISTA completa (não paginada) de serviços que correspondem à busca.
    List<Servico> findAllByNomeContainingIgnoreCase(String nome);

    List<Servico> findTop5ByOrderByIdDesc();

    @Query("SELECT new com.example.cego_webapp.dto.ServicoDesempenhoDTO(s.id, s.nome, COUNT(i.id), SUM(s.preco)) " +
            "FROM ItemServico i " +
            "JOIN i.ordemServico os " +
            "JOIN os.contaReceber cr " +
            "JOIN i.servico s " +
            "WHERE cr.status = 'RECEBIDA' " + // Puxa apenas de OS que foram pagas
            "AND cr.dataRecebimento BETWEEN :inicio AND :fim " +
            "GROUP BY s.id, s.nome " +
            "ORDER BY SUM(s.preco) DESC") // Ordena pelos mais lucrativos
    List<ServicoDesempenhoDTO> findDesempenhoServicosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}