package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.ItemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemServicoRepository extends JpaRepository<ItemServico, Integer> {
}