package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    Page<Produto> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);
    List<Produto> findTop5ByOrderByIdDesc();
}
