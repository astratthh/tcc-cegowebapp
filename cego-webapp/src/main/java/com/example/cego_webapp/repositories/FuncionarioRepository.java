package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    Page<Funcionario> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);

    Funcionario findByDocumento(String documento);

    // Retorna uma LISTA completa (não paginada) de funcionários que correspondem à busca.
    List<Funcionario> findAllByNomeContainingIgnoreCase(String nome);

    List<Funcionario> findTop5ByOrderByIdDesc();
}