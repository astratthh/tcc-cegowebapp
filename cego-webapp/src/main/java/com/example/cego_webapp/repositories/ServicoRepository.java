package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    Page<Servico> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);

    List<Servico> findTop5ByOrderByIdDesc();
}