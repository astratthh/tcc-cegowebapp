package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {
    List<Fornecedor> findByNomeContainingIgnoreCase(String term);
    Fornecedor findByDocumento(String documento);
}
