package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.CompraDTO;
import com.example.cego_webapp.models.*;
import com.example.cego_webapp.repositories.CompraRepository;
import com.example.cego_webapp.repositories.ContaPagarRepository; // NOVO IMPORT
import com.example.cego_webapp.repositories.FornecedorRepository;
import com.example.cego_webapp.repositories.ProdutoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate; // NOVO IMPORT
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/compras")
public class CompraController {

    @Autowired private CompraRepository compraRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ContaPagarRepository contaPagarRepository; // NOVO: Injetando o repositório

    @GetMapping({"", "/"})
    public String index(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCompra"));
        Page<Compra> comprasPage = compraRepository.findAll(pageable);
        model.addAttribute("comprasPage", comprasPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comprasPage.getTotalPages());

        // MUDANÇA: Adicionando dados para o novo Dashboard de Compras
        model.addAttribute("totalAPagar", contaPagarRepository.findTotalAPagar().orElse(BigDecimal.ZERO));
        model.addAttribute("totalVencido", contaPagarRepository.findTotalVencido().orElse(BigDecimal.ZERO));
        model.addAttribute("totalPagoMes", contaPagarRepository.findTotalPagoNoMes(LocalDate.now().withDayOfMonth(1)).orElse(BigDecimal.ZERO));

        model.addAttribute("activePage", "compras");
        return "compras/index";
    }

    @GetMapping("/create")
    public String createCompra(Model model) {
        if (!model.containsAttribute("compraDTO")) {
            model.addAttribute("compraDTO", new CompraDTO());
        }
        model.addAttribute("fornecedores", fornecedorRepository.findAll(Sort.by("nome")));
        model.addAttribute("produtos", produtoRepository.findAll(Sort.by("nome")));
        model.addAttribute("activePage", "compras");
        return "compras/create";
    }

    @PostMapping("/create")
    public String createCompra(@Valid @ModelAttribute CompraDTO compraDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.compraDTO", bindingResult);
            redirectAttributes.addFlashAttribute("compraDTO", compraDTO);
            return "redirect:/compras/create";
        }
        try {
            Fornecedor fornecedor = fornecedorRepository.findById(compraDTO.getFornecedorId())
                    .orElseThrow(() -> new IllegalArgumentException("Fornecedor inválido"));
            Compra compra = new Compra();
            compra.setFornecedor(fornecedor);
            compra.setDataCompra(LocalDateTime.now());
            compra.setStatus(CompraStatus.FINALIZADA);
            BigDecimal totalCompra = BigDecimal.ZERO;
            List<ItemCompra> itensCompra = new ArrayList<>();
            for (var itemDTO : compraDTO.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new IllegalArgumentException("Produto inválido"));
                produto.setEstoque(produto.getEstoque() + itemDTO.getQuantidade());
                produtoRepository.save(produto);
                ItemCompra itemCompra = new ItemCompra();
                itemCompra.setCompra(compra);
                itemCompra.setProduto(produto);
                itemCompra.setQuantidade(itemDTO.getQuantidade());
                itemCompra.setCustoUnitario(itemDTO.getCustoUnitario());
                itensCompra.add(itemCompra);
                totalCompra = totalCompra.add(itemDTO.getCustoUnitario().multiply(new BigDecimal(itemDTO.getQuantidade())));
            }
            compra.setItens(itensCompra);
            compra.setValorTotal(totalCompra);
            ContaPagar contaPagar = new ContaPagar();
            contaPagar.setCompra(compra);
            contaPagar.setValor(totalCompra);
            contaPagar.setDataVencimento(compraDTO.getDataVencimento());
            contaPagar.setStatus(ContaPagarStatus.A_PAGAR);
            compra.setContaPagar(contaPagar);
            compraRepository.save(compra);
            redirectAttributes.addFlashAttribute("successMessage", "Compra #" + compra.getId() + " registrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao registrar a compra: " + e.getMessage());
            return "redirect:/compras/create";
        }
        return "redirect:/compras";
    }

    @GetMapping("/cancelar")
    public String cancelarCompra(@RequestParam Long id, @RequestParam(required = false, defaultValue = "Cancelado pelo usuário") String motivo, RedirectAttributes redirectAttributes) {
        try {
            Compra compra = compraRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Compra não encontrada!"));
            if (compra.getContaPagar() == null || compra.getContaPagar().getStatus() != ContaPagarStatus.A_PAGAR) {
                redirectAttributes.addFlashAttribute("errorMessage", "Não é possível cancelar uma compra que já foi paga ou cancelada.");
                return "redirect:/compras";
            }
            for (ItemCompra item : compra.getItens()) {
                Produto produto = item.getProduto();
                if (produto.getEstoque() < item.getQuantidade()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Cancelamento bloqueado: o produto '" + produto.getNome() + "' não tem estoque suficiente para a devolução (itens já podem ter sido vendidos).");
                    return "redirect:/compras";
                }
            }
            for (ItemCompra item : compra.getItens()) {
                Produto produto = item.getProduto();
                produto.setEstoque(produto.getEstoque() - item.getQuantidade());
                produtoRepository.save(produto);
            }
            compra.setStatus(CompraStatus.CANCELADA);
            compra.setMotivoCancelamento(motivo);
            compra.setDataCancelamento(LocalDateTime.now());
            compra.getContaPagar().setStatus(ContaPagarStatus.CANCELADA);
            compraRepository.save(compra);
            redirectAttributes.addFlashAttribute("successMessage", "Compra #" + id + " cancelada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao cancelar a compra: " + e.getMessage());
        }
        return "redirect:/compras";
    }
}