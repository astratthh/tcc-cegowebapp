// src/main/java/com/example/cego_webapp/services/ServicoService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.ServicoDTO;
import com.example.cego_webapp.models.Servico;
import com.example.cego_webapp.repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository servicoRepository;

    public Page<Servico> listarServicos(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return servicoRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            return servicoRepository.findAll(pageable);
        }
    }

    public List<Servico> listarTodosParaRelatorio(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return servicoRepository.findAllByNomeContainingIgnoreCase(keyword);
        } else {
            return servicoRepository.findAll();
        }
    }

    public Servico buscarPorId(Integer id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço com ID " + id + " não encontrado."));
    }

    @Transactional
    public Servico criarServico(ServicoDTO servicoDTO) {
        Servico servico = new Servico();
        servico.setNome(servicoDTO.getNome());
        servico.setDescricao(servicoDTO.getDescricao());
        servico.setPreco(servicoDTO.getPreco());
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizarServico(Integer id, ServicoDTO servicoDTO) {
        Servico servico = buscarPorId(id); // Reutiliza o método de busca

        servico.setNome(servicoDTO.getNome());
        servico.setDescricao(servicoDTO.getDescricao());
        servico.setPreco(servicoDTO.getPreco());
        return servicoRepository.save(servico);
    }

    @Transactional
    public void deletarServico(Integer id) {
        if (!servicoRepository.existsById(id)) {
            throw new EntityNotFoundException("Serviço não encontrado.");
        }
        // try-catch removido
        servicoRepository.deleteById(id);
    }
}