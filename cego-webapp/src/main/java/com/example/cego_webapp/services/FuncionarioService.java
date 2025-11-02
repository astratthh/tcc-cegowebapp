package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.FuncionarioDTO;
import com.example.cego_webapp.models.Funcionario;
import com.example.cego_webapp.repositories.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // Adicione este import

@Service
public class FuncionarioService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public Page<Funcionario> listarFuncionarios(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return funcionarioRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            return funcionarioRepository.findAll(pageable);
        }
    }

    public List<Funcionario> listarTodosParaRelatorio(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return funcionarioRepository.findAllByNomeContainingIgnoreCase(keyword);
        } else {
            return funcionarioRepository.findAll();
        }
    }

    public Funcionario buscarPorId(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário com ID " + id + " não encontrado."));
    }

    @Transactional
    public Funcionario criarFuncionario(FuncionarioDTO funcionarioDTO) {
        String documentoLimpo = funcionarioDTO.getDocumento().replaceAll("[^0-9]", "");
        if (funcionarioRepository.findByDocumento(documentoLimpo) != null) {
            throw new IllegalArgumentException("Documento já cadastrado.");
        }
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setDocumento(documentoLimpo);
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setTelefone(funcionarioDTO.getTelefone());
        funcionario.setEndereco(funcionarioDTO.getEndereco());
        funcionario.setCargo(funcionarioDTO.getCargo());
        funcionario.setSalario(funcionarioDTO.getSalario());
        return funcionarioRepository.save(funcionario);
    }

    @Transactional
    public Funcionario atualizarFuncionario(Integer id, FuncionarioDTO funcionarioDTO) {
        Funcionario funcionario = buscarPorId(id);
        String documentoLimpo = funcionarioDTO.getDocumento().replaceAll("[^0-9]", "");
        Funcionario funcionarioExistente = funcionarioRepository.findByDocumento(documentoLimpo);
        if (funcionarioExistente != null && !funcionarioExistente.getId().equals(id)) {
            throw new IllegalArgumentException("Documento já cadastrado em outro funcionário.");
        }
        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setDocumento(documentoLimpo);
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setTelefone(funcionarioDTO.getTelefone());
        funcionario.setEndereco(funcionarioDTO.getEndereco());
        funcionario.setCargo(funcionarioDTO.getCargo());
        funcionario.setSalario(funcionarioDTO.getSalario());
        return funcionarioRepository.save(funcionario);
    }

    @Transactional
    public void deletarFuncionario(Integer id) {
        if (!funcionarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Funcionário não encontrado.");
        }
        funcionarioRepository.deleteById(id);
    }
}