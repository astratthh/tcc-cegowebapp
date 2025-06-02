package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
}
