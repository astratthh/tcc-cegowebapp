package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer> {

    // O JpaRepository já nos fornece os seguintes métodos que estamos usando no controller:
    // - save() -> para criar e atualizar uma OS
    // - findById() -> para buscar uma OS específica
    // - findAll(Pageable pageable) -> para listar todas as OS com paginação

    // No futuro, se você precisar de uma busca mais específica, poderá adicioná-la aqui. Exemplo:
    // Page<OrdemServico> findByVeiculoPlacaContainingIgnoreCase(String placa, Pageable pageable);
}