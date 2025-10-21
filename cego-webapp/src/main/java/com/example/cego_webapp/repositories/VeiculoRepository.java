package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {

    Veiculo findByPlaca(String placa);

    @Query("SELECT v FROM Veiculo v WHERE v.cliente.id = :clienteId")
    List<Veiculo> findByClienteId(@Param("clienteId") Integer clienteId);

    // Método para a paginação (já existe)
    Page<Veiculo> findByPlacaContainingIgnoreCase(String keyword, Pageable pageable);

    // ### NOVO MÉTODO PARA O RELATÓRIO ###
    // Retorna uma LISTA completa (não paginada) de veículos que correspondem à busca pela placa.
    List<Veiculo> findAllByPlacaContainingIgnoreCase(String placa);
}