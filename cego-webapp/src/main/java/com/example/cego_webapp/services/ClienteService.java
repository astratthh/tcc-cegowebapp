// src/main/java/com/example/cego_webapp/services/ClienteService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.ClienteDTO;
import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.repositories.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Lógica para listar e buscar clientes
    public Page<Cliente> listarClientes(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return clienteRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            return clienteRepository.findAll(pageable);
        }
    }

    public List<Cliente> listarTodosParaRelatorio(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return clienteRepository.findAllByNomeContainingIgnoreCase(keyword);
        } else {
            return clienteRepository.findAll();
        }
    }

    // Lógica para buscar um cliente por ID
    public Cliente buscarPorId(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente com ID " + id + " não encontrado."));
    }

    // Lógica para criar um novo cliente
    @Transactional
    public Cliente criarCliente(ClienteDTO clienteDTO) {
        String documentoLimpo = clienteDTO.getDocumento() != null ? clienteDTO.getDocumento().replaceAll("[^0-9]", "") : "";

        if (clienteRepository.findByDocumento(documentoLimpo) != null) {
            throw new IllegalArgumentException("Documento já cadastrado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDTO.getNome());
        cliente.setDocumento(documentoLimpo);
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefone(clienteDTO.getTelefone());
        cliente.setEndereco(clienteDTO.getEndereco());

        return clienteRepository.save(cliente);
    }

    // Lógica para atualizar um cliente
    @Transactional
    public Cliente atualizarCliente(Integer id, ClienteDTO clienteDTO) {
        Cliente cliente = buscarPorId(id);

        String documentoLimpo = clienteDTO.getDocumento().replaceAll("[^0-9]", "");
        Cliente clienteExistenteComDocumento = clienteRepository.findByDocumento(documentoLimpo);

        if (clienteExistenteComDocumento != null && !clienteExistenteComDocumento.getId().equals(id)) {
            throw new IllegalArgumentException("Documento já cadastrado em outro cliente.");
        }

        cliente.setNome(clienteDTO.getNome());
        cliente.setDocumento(documentoLimpo);
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefone(clienteDTO.getTelefone());
        cliente.setEndereco(clienteDTO.getEndereco());

        return clienteRepository.save(cliente);
    }

    // Lógica para deletar um cliente
    @Transactional
    public void deletarCliente(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente com ID " + id + " não encontrado.");
        }

        clienteRepository.deleteById(id);
    }
}