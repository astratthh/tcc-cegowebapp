package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    // Este método já existe e é usado para a paginação
    Page<Funcionario> findByNomeContainingIgnoreCase(String keyword, Pageable pageable);

    Funcionario findByDocumento(String documento);

    // ### NOVO MÉTODO PARA O RELATÓRIO ###
    // Retorna uma LISTA completa (não paginada) de funcionários que correspondem à busca.
    List<Funcionario> findAllByNomeContainingIgnoreCase(String nome);

    // (Você pode remover 'List<Funcionario> findByNomeContainingIgnoreCase(String term)' se não estiver usando em outro lugar para evitar duplicidade)
    List<Funcionario> findTop5ByOrderByIdDesc();
}