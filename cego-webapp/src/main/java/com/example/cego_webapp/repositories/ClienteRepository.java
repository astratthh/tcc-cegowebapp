package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByNomeContainingIgnoreCase(String term);
    Cliente findByDocumento(String documento);
    Page<Cliente> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);
    List<Cliente> findTop5ByOrderByIdDesc();
}
