// src/main/java/com/example/cego_webapp/services/FornecedorService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.FornecedorDTO;
import com.example.cego_webapp.models.Fornecedor;
import com.example.cego_webapp.repositories.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    public Page<Fornecedor> listarFornecedores(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return fornecedorRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            return fornecedorRepository.findAll(pageable);
        }
    }

    public List<Fornecedor> listarTodosParaRelatorio(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return fornecedorRepository.findAllByNomeContainingIgnoreCase(keyword);
        } else {
            return fornecedorRepository.findAll();
        }
    }

    public Fornecedor buscarPorId(Integer id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor com ID " + id + " não encontrado."));
    }

    @Transactional
    public Fornecedor criarFornecedor(FornecedorDTO fornecedorDTO) {
        String documentoLimpo = fornecedorDTO.getDocumento().replaceAll("[^0-9]", "");
        if (fornecedorRepository.findByDocumento(documentoLimpo) != null) {
            throw new IllegalArgumentException("Documento já cadastrado.");
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(fornecedorDTO.getNome());
        fornecedor.setDocumento(documentoLimpo);
        fornecedor.setEmail(fornecedorDTO.getEmail());
        fornecedor.setTelefone(fornecedorDTO.getTelefone());
        fornecedor.setEndereco(fornecedorDTO.getEndereco());

        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizarFornecedor(Integer id, FornecedorDTO fornecedorDTO) {
        Fornecedor fornecedor = buscarPorId(id);

        String documentoLimpo = fornecedorDTO.getDocumento().replaceAll("[^0-9]", "");
        Fornecedor fornecedorExistente = fornecedorRepository.findByDocumento(documentoLimpo);

        // A verificação correta: se um fornecedor com este documento já existe E não é o fornecedor que estamos editando
        if (fornecedorExistente != null && !fornecedorExistente.getId().equals(id)) {
            throw new IllegalArgumentException("Documento já cadastrado em outro fornecedor.");
        }

        fornecedor.setNome(fornecedorDTO.getNome());
        fornecedor.setDocumento(documentoLimpo);
        fornecedor.setEmail(fornecedorDTO.getEmail());
        fornecedor.setTelefone(fornecedorDTO.getTelefone());
        fornecedor.setEndereco(fornecedorDTO.getEndereco());

        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public void deletarFornecedor(Integer id) {
        if (!fornecedorRepository.existsById(id)) {
            throw new EntityNotFoundException("Fornecedor não encontrado.");
        }
        fornecedorRepository.deleteById(id);
    }
}