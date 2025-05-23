package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    public Cliente findByEmail(String email);
    List<Cliente> findByNomeContainingIgnoreCase(String term);
}
