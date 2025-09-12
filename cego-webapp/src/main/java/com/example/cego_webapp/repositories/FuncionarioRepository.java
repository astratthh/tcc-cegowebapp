package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.models.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {
    List<Funcionario> findByNomeContainingIgnoreCase(String term);
    Funcionario findByDocumento(String documento);
    Page<Funcionario> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);
    List<Funcionario> findTop5ByOrderByIdDesc();
}
