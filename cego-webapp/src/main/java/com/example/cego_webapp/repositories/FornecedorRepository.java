package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {

    // Método para a paginação (já existe)
    Page<Fornecedor> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);

    Fornecedor findByDocumento(String documento);

    // ### NOVO MÉTODO PARA O RELATÓRIO ###
    // Retorna uma LISTA completa, não uma página.
    List<Fornecedor> findAllByNomeContainingIgnoreCase(String nome);
}