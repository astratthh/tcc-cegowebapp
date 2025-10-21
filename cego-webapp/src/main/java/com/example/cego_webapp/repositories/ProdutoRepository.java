package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    // Método para a paginação (já existe)
    Page<Produto> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);

    // ### NOVO MÉTODO PARA O RELATÓRIO ###
    // Retorna uma LISTA completa (não paginada) de produtos que correspondem à busca.
    List<Produto> findAllByNomeContainingIgnoreCase(String nome);

    List<Produto> findTop5ByOrderByIdDesc();
}