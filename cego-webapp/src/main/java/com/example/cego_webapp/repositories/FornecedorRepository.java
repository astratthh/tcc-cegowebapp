package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {
    List<Fornecedor> findByNomeContainingIgnoreCase(String term);
    Fornecedor findByDocumento(String documento);
    Page<Fornecedor> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);
}
