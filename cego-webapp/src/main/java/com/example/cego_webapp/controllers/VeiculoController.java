//package com.example.cego_webapp.controllers;
//
//import com.example.cego_webapp.dto.VeiculoDTO;
//import com.example.cego_webapp.models.Cliente;
//import com.example.cego_webapp.models.Veiculo;
//import com.example.cego_webapp.repositories.ClienteRepository;
//import com.example.cego_webapp.repositories.VeiculoRepository;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/veiculos")
//public class VeiculoController {
//
//    @Autowired
//    VeiculoRepository veiculoRepository;
//
//    @Autowired
//    ClienteRepository clienteRepository;
//
//    @GetMapping({"", "/"})
//    public String getVeiculos(Model model,
//                              @RequestParam(defaultValue = "0") int page,
//                              @RequestParam(defaultValue = "13") int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
//        Page<Veiculo> veiculosPage = veiculoRepository.findAll(pageable);
//
//        model.addAttribute("veiculosPage", veiculosPage);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", veiculosPage.getTotalPages());
//
//        return "veiculos/index";
//    }
//
//    @GetMapping("/cliente/{clienteId}")
//    public String getVeiculosByCliente(@PathVariable Integer clienteId, Model model) {
//        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
//        if (cliente == null) {
//            return "redirect:/clientes";
//        }
//        List<Veiculo> veiculos = veiculoRepository.findByClienteId(clienteId);
//        model.addAttribute("cliente", cliente);
//        model.addAttribute("veiculos", veiculos);
//        return "veiculos/listaPorCliente";
//    }
//
//    @GetMapping("/create")
//    public String createVeiculo(Model model) {
//        VeiculoDTO veiculoDTO = new VeiculoDTO();
//        model.addAttribute("veiculoDTO", veiculoDTO);
//        List<Cliente> clientes = clienteRepository.findAll();
//        model.addAttribute("clientes", clientes);
//        return "veiculos/create";
//    }
//
//    @PostMapping("/create")
//    public String createVeiculo(@Valid @ModelAttribute VeiculoDTO veiculoDTO, BindingResult bindingResult, Model model) {
//
//        // Validação de placa única
//        if (veiculoRepository.findByPlaca(veiculoDTO.getPlaca()) != null) {
//            bindingResult.addError(new FieldError(
//                    "veiculoDTO", "placa", "Placa já cadastrada"
//            ));
//        }
//
//        if (bindingResult.hasErrors()) {
//            List<Cliente> clientes = clienteRepository.findAll();
//            model.addAttribute("clientes", clientes);
//            return "veiculos/create";
//        }
//
//        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId()).orElse(null);
//        if (cliente == null) {
//            bindingResult.addError(new FieldError(
//                    "veiculoDTO", "clienteId", "Cliente não encontrado"
//            ));
//            List<Cliente> clientes = clienteRepository.findAll();
//            model.addAttribute("clientes", clientes);
//            return "veiculos/create";
//        }
//
//        Veiculo veiculo = new Veiculo();
//        veiculo.setPlaca(veiculoDTO.getPlaca());
//        veiculo.setMarca(veiculoDTO.getMarca());
//        veiculo.setModelo(veiculoDTO.getModelo());
//        veiculo.setAno(veiculoDTO.getAno());
//        veiculo.setCliente(cliente);
//
//        veiculoRepository.save(veiculo);
//
//        return "redirect:/veiculos/";
//    }
//
//    @GetMapping("/edit")
//    public String editVeiculo(@RequestParam Integer id, Model model) {
//        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
//        if (veiculo == null) {
//            return "redirect:/veiculos";
//        }
//
//        VeiculoDTO veiculoDTO = new VeiculoDTO();
//        veiculoDTO.setPlaca(veiculo.getPlaca());
//        veiculoDTO.setMarca(veiculo.getMarca());
//        veiculoDTO.setModelo(veiculo.getModelo());
//        veiculoDTO.setAno(veiculo.getAno());
//        veiculoDTO.setClienteId(veiculo.getCliente().getId()); // Preenche o ID do cliente
//
//        model.addAttribute("veiculo", veiculo);
//        model.addAttribute("veiculoDTO", veiculoDTO);
//        List<Cliente> clientes = clienteRepository.findAll();
//        model.addAttribute("clientes", clientes);
//
//        return "veiculos/edit";
//    }
//
//    @PostMapping("/edit")
//    public String editVeiculo(
//            Model model,
//            @RequestParam Integer id,
//            @Valid @ModelAttribute VeiculoDTO veiculoDTO,
//            BindingResult bindingResult
//    ) {
//        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
//        if (veiculo == null) {
//            return "redirect:/veiculos";
//        }
//
//        if (!veiculoDTO.getPlaca().equals(veiculo.getPlaca())) {
//            Veiculo existing = veiculoRepository.findByPlaca(veiculoDTO.getPlaca());
//            if (existing != null) {
//                bindingResult.addError(new FieldError(
//                        "veiculoDTO", "placa", "Placa já cadastrada"
//                ));
//            }
//        }
//
//        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId()).orElse(null);
//        if (cliente == null) {
//            bindingResult.addError(new FieldError(
//                    "veiculoDTO", "clienteId", "Cliente não encontrado"
//            ));
//        }
//
//        model.addAttribute("veiculo", veiculo);
//        if (bindingResult.hasErrors()) {
//            List<Cliente> clientes = clienteRepository.findAll();
//            model.addAttribute("clientes", clientes);
//            return "veiculos/edit";
//        }
//
//        veiculo.setPlaca(veiculoDTO.getPlaca());
//        veiculo.setMarca(veiculoDTO.getMarca());
//        veiculo.setModelo(veiculoDTO.getModelo());
//        veiculo.setAno(veiculoDTO.getAno());
//        veiculo.setCliente(cliente);
//        veiculoRepository.save(veiculo);
//
//        return "redirect:/veiculos";
//    }
//
//    @GetMapping("/delete")
//    public String deleteVeiculo(@RequestParam Integer id) {
//        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
//        if (veiculo != null) {
//            veiculoRepository.delete(veiculo);
//        }
//        return "redirect:/veiculos";
//    }
//}

package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.VeiculoDTO;
import com.example.cego_webapp.models.Cliente;
import com.example.cego_webapp.models.Veiculo;
import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.VeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

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
                              @RequestParam(defaultValue = "13") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Veiculo> veiculosPage = veiculoRepository.findAll(pageable);

        model.addAttribute("veiculosPage", veiculosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", veiculosPage.getTotalPages());

        return "veiculos/index";
    }

    @GetMapping("/create")
    public String createVeiculo(Model model) {
        VeiculoDTO veiculoDTO = new VeiculoDTO();
        model.addAttribute("veiculoDTO", veiculoDTO);
        List<Cliente> clientes = clienteRepository.findAll();
        model.addAttribute("clientes", clientes);
        return "veiculos/create";
    }

    @PostMapping("/create")
    public String createVeiculo(@Valid @ModelAttribute VeiculoDTO veiculoDTO, BindingResult bindingResult, Model model) {

        if (!bindingResult.hasErrors()) {
            String placaLimpa = veiculoDTO.getPlaca() != null ? veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase() : "";

            // Validação de formato de placa
            if (!isValidBrazilianPlate(placaLimpa)) {
                bindingResult.addError(new FieldError(
                        "veiculoDTO", "placa", "Formato de placa inválido (ex: AAA0000, AAA0A00, AAA00A0)"
                ));
            }

            // Validação de placa única (só se o formato for válido)
            if (!bindingResult.hasErrors() && veiculoRepository.findByPlaca(placaLimpa) != null) {
                bindingResult.addError(new FieldError(
                        "veiculoDTO", "placa", "Placa já cadastrada"
                ));
            }
        }

        if (bindingResult.hasErrors()) {
            List<Cliente> clientes = clienteRepository.findAll();
            model.addAttribute("clientes", clientes);
            return "veiculos/create";
        }

        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId()).orElse(null);
        if (cliente == null) {
            bindingResult.addError(new FieldError(
                    "veiculoDTO", "clienteId", "Cliente não encontrado"
            ));
            List<Cliente> clientes = clienteRepository.findAll();
            model.addAttribute("clientes", clientes);
            return "veiculos/create";
        }

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase()); // Salva a placa limpa e padronizada
        veiculo.setMarca(veiculoDTO.getMarca());
        veiculo.setModelo(veiculoDTO.getModelo());
        veiculo.setAno(veiculoDTO.getAno());
        veiculo.setCliente(cliente);

        veiculoRepository.save(veiculo);

        return "redirect:/veiculos/";
    }

    @GetMapping("/edit")
    public String editVeiculo(@RequestParam Integer id, Model model) {
        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
        if (veiculo == null) {
            return "redirect:/veiculos";
        }

        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca(veiculo.getPlaca());
        veiculoDTO.setMarca(veiculo.getMarca());
        veiculoDTO.setModelo(veiculo.getModelo());
        veiculoDTO.setAno(veiculo.getAno());
        veiculoDTO.setClienteId(veiculo.getCliente().getId());

        model.addAttribute("veiculo", veiculo);
        model.addAttribute("veiculoDTO", veiculoDTO);
        List<Cliente> clientes = clienteRepository.findAll();
        model.addAttribute("clientes", clientes);

        return "veiculos/edit";
    }

    @PostMapping("/edit")
    public String editVeiculo(
            Model model,
            @RequestParam Integer id,
            @Valid @ModelAttribute VeiculoDTO veiculoDTO,
            BindingResult bindingResult
    ) {
        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
        if (veiculo == null) {
            return "redirect:/veiculos";
        }

        // Se já houver erros de validação (ex: @NotBlank), não prossegue com as validações customizadas
        if (!bindingResult.hasErrors()) {
            String placaLimpa = veiculoDTO.getPlaca() != null ? veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase() : "";

            // Validação de formato de placa
            if (!isValidBrazilianPlate(placaLimpa)) {
                bindingResult.addError(new FieldError(
                        "veiculoDTO", "placa", "Formato de placa inválido (ex: AAA0000, AAA0A00, AAA00A0)"
                ));
            }

            // Validação de placa única (se alterada e só se o formato for válido)
            if (!bindingResult.hasErrors() && !placaLimpa.equals(veiculo.getPlaca())) {
                Veiculo existing = veiculoRepository.findByPlaca(placaLimpa);
                if (existing != null) {
                    bindingResult.addError(new FieldError(
                            "veiculoDTO", "placa", "Placa já cadastrada"
                    ));
                }
            }
        }

        Cliente cliente = clienteRepository.findById(veiculoDTO.getClienteId()).orElse(null);
        if (cliente == null) {
            bindingResult.addError(new FieldError(
                    "veiculoDTO", "clienteId", "Cliente não encontrado"
            ));
        }

        model.addAttribute("veiculo", veiculo);
        if (bindingResult.hasErrors()) {
            List<Cliente> clientes = clienteRepository.findAll();
            model.addAttribute("clientes", clientes);
            return "veiculos/edit";
        }

        veiculo.setPlaca(veiculoDTO.getPlaca().replaceAll("-", "").toUpperCase()); // Salva a placa limpa e padronizada
        veiculo.setMarca(veiculoDTO.getMarca());
        veiculo.setModelo(veiculoDTO.getModelo());
        veiculo.setAno(veiculoDTO.getAno());
        veiculo.setCliente(cliente);
        veiculoRepository.save(veiculo);

        return "redirect:/veiculos";
    }

    @GetMapping("/delete")
    public String deleteVeiculo(@RequestParam Integer id) {
        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);
        if (veiculo != null) {
            veiculoRepository.delete(veiculo);
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

