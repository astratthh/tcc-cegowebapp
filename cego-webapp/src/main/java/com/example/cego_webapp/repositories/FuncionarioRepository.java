package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {
    List<Funcionario> findByNomeContainingIgnoreCase(String term);
    Funcionario findByDocumento(String documento);
}
