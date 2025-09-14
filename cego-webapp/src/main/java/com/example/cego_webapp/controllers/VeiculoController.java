package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.VeiculoDTO;
import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.models.Veiculo;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.VeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    VeiculoRepository veiculoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    private static final String PLATE_REGEX = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$|^[A-Z]{3}[0-9]{2}[A-Z][0-9]$";
    private static final Pattern PLATE_PATTERN = Pattern.compile(PLATE_REGEX);

    @GetMapping({"", "/"})
    public String getVeiculos(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id,desc") String sort,
                              @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));
        Page<Veiculo> veiculosPage;

        if(keyword != null && !keyword.isEmpty()){
            veiculosPage = veiculoRepository.findByPlacaContainingIgnoreCase(keyword, pageable);
        } else {
            veiculosPage = veiculoRepository.findAll(pageable);
        }

        model.addAttribute("veiculosPage", veiculosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", veiculosPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);

        return "veiculos/index";
    }

    @GetMapping("/create")
    public String createVeiculo(Model model) {
        if (!model.containsAttribute("veiculoDTO")) {
            model.addAttribute("veiculoDTO", new VeiculoDTO());
        }
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));
        return "veiculos/create";
    }

    @PostMapping("/create")
    public String createVeiculo(@Valid @ModelAttribute("veiculoDTO") VeiculoDTO veiculoDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (!bindingResult.hasErrors()) {
            String placaLimpa = veiculoDTO.getPlaca() != null ? veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase() : "";

            if (!isValidBrazilianPlate(placaLimpa)) {
                bindingResult.addError(new FieldError("veiculoDTO", "placa", "Formato de placa inválido (ex: AAA0000)"));
            }

            if (!bindingResult.hasErrors() && veiculoRepository.findByPlaca(placaLimpa) != null) {
                bindingResult.addError(new FieldError("veiculoDTO", "placa", "Placa já cadastrada"));
            }
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.veiculoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("veiculoDTO", veiculoDTO);
            return "redirect:/veiculos/create";
        }

        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId()).orElse(null);
        if (cliente == null) {
            bindingResult.addError(new FieldError("veiculoDTO", "clienteId", "Cliente não encontrado"));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.veiculoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("veiculoDTO", veiculoDTO);
            return "redirect:/veiculos/create";
        }

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase());
        veiculo.setMarca(veiculoDTO.getMarca());
        veiculo.setModelo(veiculoDTO.getModelo());
        veiculo.setAno(veiculoDTO.getAno());
        veiculo.setCliente(cliente);

        veiculoRepository.save(veiculo);

        redirectAttributes.addFlashAttribute("successMessage", "Veículo cadastrado com sucesso!");
        return "redirect:/veiculos";
    }

    @GetMapping("/edit")
    public String editVeiculo(@RequestParam Integer id, Model model) {
        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
        if (veiculo == null) {
            return "redirect:/veiculos";
        }

        if (!model.containsAttribute("veiculoDTO")) {
            VeiculoDTO veiculoDTO = new VeiculoDTO();
            veiculoDTO.setPlaca(veiculo.getPlaca());
            veiculoDTO.setMarca(veiculo.getMarca());
            veiculoDTO.setModelo(veiculo.getModelo());
            veiculoDTO.setAno(veiculo.getAno());
            veiculoDTO.setClienteId(veiculo.getCliente().getId());
            model.addAttribute("veiculoDTO", veiculoDTO);
        }

        model.addAttribute("veiculo", veiculo);
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by("nome")));

        return "veiculos/edit";
    }

    @PostMapping("/edit")
    public String editVeiculo(@RequestParam Integer id, @Valid @ModelAttribute("veiculoDTO") VeiculoDTO veiculoDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
        if (veiculo == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Veículo não encontrado.");
            return "redirect:/veiculos";
        }

        if (!bindingResult.hasErrors()) {
            String placaLimpa = veiculoDTO.getPlaca() != null ? veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase() : "";

            if (!isValidBrazilianPlate(placaLimpa)) {
                bindingResult.addError(new FieldError("veiculoDTO", "placa", "Formato de placa inválido (ex: AAA0000)"));
            }

            if (!bindingResult.hasErrors() && !placaLimpa.equalsIgnoreCase(veiculo.getPlaca())) {
                if (veiculoRepository.findByPlaca(placaLimpa) != null) {
                    bindingResult.addError(new FieldError("veiculoDTO", "placa", "Placa já cadastrada"));
                }
            }
        }

        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId()).orElse(null);
        if (cliente == null) {
            bindingResult.addError(new FieldError("veiculoDTO", "clienteId", "Cliente não encontrado"));
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.veiculoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("veiculoDTO", veiculoDTO);
            return "redirect:/veiculos/edit?id=" + id;
        }

        veiculo.setPlaca(veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase());
        veiculo.setMarca(veiculoDTO.getMarca());
        veiculo.setModelo(veiculoDTO.getModelo());
        veiculo.setAno(veiculoDTO.getAno());
        veiculo.setCliente(cliente);
        veiculoRepository.save(veiculo);

        redirectAttributes.addFlashAttribute("successMessage", "Veículo atualizado com sucesso!");
        return "redirect:/veiculos";
    }

    @GetMapping("/delete")
    public String deleteVeiculo(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            if (!veiculoRepository.existsById(id)) {
                throw new Exception("Veículo não encontrado.");
            }
            veiculoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Veículo excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o veículo, pois ele está associado a uma ou mais Ordens de Serviço.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir o veículo: " + e.getMessage());
        }
        return "redirect:/veiculos";
    }

    private boolean isValidBrazilianPlate(String placa) {
        if (placa == null || placa.isEmpty()) {
            return false;
        }
        String cleanedPlaca = placa.replaceAll("-", "").toUpperCase();
        Matcher matcher = PLATE_PATTERN.matcher(cleanedPlaca);
        return matcher.matches();
    }
}