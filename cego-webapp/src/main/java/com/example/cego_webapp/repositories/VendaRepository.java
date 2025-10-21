package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "itens", "itens.produto"})
    Page<Venda> findAll(Pageable pageable);

    Page<Venda> findByClienteNomeContainingIgnoreCase(String keyword, Pageable pageable);
}