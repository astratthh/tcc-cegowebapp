// src/main/java/com/example/cego_webapp/services/VeiculoService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.VeiculoDTO;
import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.models.Veiculo;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.VeiculoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    private static final String PLATE_REGEX = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$";
    private static final Pattern PLATE_PATTERN = Pattern.compile(PLATE_REGEX);

    private boolean isValidBrazilianPlate(String placa) {
        if (placa == null || placa.isEmpty()) return false;
        Matcher matcher = PLATE_PATTERN.matcher(placa);
        return matcher.matches();
    }

    public Page<Veiculo> listarVeiculos(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return veiculoRepository.findByPlacaContainingIgnoreCase(keyword, pageable);
        } else {
            return veiculoRepository.findAll(pageable);
        }
    }

    public List<Veiculo> listarTodosParaRelatorio(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return veiculoRepository.findAllByPlacaContainingIgnoreCase(keyword);
        } else {
            return veiculoRepository.findAll();
        }
    }

    public Veiculo buscarPorId(Integer id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Veículo com ID " + id + " não encontrado."));
    }

    @Transactional
    public Veiculo criarVeiculo(VeiculoDTO veiculoDTO) {
        String placaLimpa = veiculoDTO.getPlaca() != null ? veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase() : "";

        if (!isValidBrazilianPlate(placaLimpa)) {
            throw new IllegalArgumentException("Formato de placa inválido (ex: ABC1234 ou ABC1D23).");
        }
        if (veiculoRepository.findByPlaca(placaLimpa) != null) {
            throw new IllegalArgumentException("Placa já cadastrada.");
        }

        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placaLimpa);
        veiculo.setMarca(veiculoDTO.getMarca());
        veiculo.setModelo(veiculoDTO.getModelo());
        veiculo.setAno(veiculoDTO.getAno());
        veiculo.setCliente(cliente);

        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizarVeiculo(Integer id, VeiculoDTO veiculoDTO) {
        Veiculo veiculo = buscarPorId(id);
        String placaLimpa = veiculoDTO.getPlaca() != null ? veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase() : "";

        if (!isValidBrazilianPlate(placaLimpa)) {
            throw new IllegalArgumentException("Formato de placa inválido (ex: ABC1234 ou ABC1D23).");
        }

        // Verifica se a placa foi alterada e se a nova placa já existe em outro veículo
        if (!placaLimpa.equalsIgnoreCase(veiculo.getPlaca())) {
            if (veiculoRepository.findByPlaca(placaLimpa) != null) {
                throw new IllegalArgumentException("Placa já cadastrada em outro veículo.");
            }
        }

        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        veiculo.setPlaca(placaLimpa);
        veiculo.setMarca(veiculoDTO.getMarca());
        veiculo.setModelo(veiculoDTO.getModelo());
        veiculo.setAno(veiculoDTO.getAno());
        veiculo.setCliente(cliente);

        return veiculoRepository.save(veiculo);
    }

    public List<Veiculo> listarPorCliente(Integer clienteId) {
        return veiculoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public void deletarVeiculo(Integer id) {
        if (!veiculoRepository.existsById(id)) {
            throw new EntityNotFoundException("Veículo não encontrado.");
        }
        veiculoRepository.deleteById(id);
    }
}