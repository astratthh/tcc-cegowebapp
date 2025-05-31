package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {
    Veiculo findByPlaca(String placa);
    List<Veiculo> findByClienteId(Integer clienteId);
}
