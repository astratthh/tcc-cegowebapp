// src/main/java/com/example/cego_webapp/controllers/ProdutoController.java
package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ProdutoDTO;
import com.example.cego_webapp.models.Produto;
import com.example.cego_webapp.services.PdfService;
import com.example.cego_webapp.services.ProdutoService; // NOVO IMPORT
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService; // INJETA O SERVICE

    @Autowired
    private PdfService pdfService;

    @GetMapping({"", "/"})
    public String getProdutos(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id,desc") String sort,
                              @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDir = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortField));

        Page<Produto> produtosPage = produtoService.listarProdutos(keyword, pageable);

        model.addAttribute("produtosPage", produtosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", produtosPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir.name().toLowerCase());
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "produtos");

        return "produtos/index";
    }

    @GetMapping("/create")
    public String createProdutoForm(Model model) {
        if (!model.containsAttribute("produtoDTO")) {
            model.addAttribute("produtoDTO", new ProdutoDTO());
        }
        model.addAttribute("activePage", "produtos");
        return "produtos/create";
    }

    @PostMapping("/create")
    public String createProduto(@Valid @ModelAttribute("produtoDTO") ProdutoDTO produtoDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.produtoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("produtoDTO", produtoDTO);
            return "redirect:/produtos/create";
        }

        produtoService.criarProduto(produtoDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Produto cadastrado com sucesso!");
        return "redirect:/produtos";
    }

    @GetMapping("/edit")
    public String editProdutoForm(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoService.buscarPorId(id);
            if (!model.containsAttribute("produtoDTO")) {
                // Adicione este construtor ao seu ProdutoDTO
                model.addAttribute("produtoDTO", new ProdutoDTO(produto));
            }
            model.addAttribute("produto", produto);
            model.addAttribute("activePage", "produtos");
            return "produtos/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/produtos";
        }
    }

    @PostMapping("/edit")
    public String editProduto(@RequestParam Integer id, @Valid @ModelAttribute("produtoDTO") ProdutoDTO produtoDTO,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.produtoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("produtoDTO", produtoDTO);
            return "redirect:/produtos/edit?id=" + id;
        }

        try {
            produtoService.atualizarProduto(id, produtoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Produto atualizado com sucesso!");
            return "redirect:/produtos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("produtoDTO", produtoDTO);
            return "redirect:/produtos/edit?id=" + id;
        }
    }

    @GetMapping("/delete")
    public String deleteProduto(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.deletarProduto(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produto excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // "Tradução" do erro
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o produto, pois ele está associado a Vendas ou Compras.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/produtos";
    }

    @GetMapping("/relatorio/pdf")
    public void gerarRelatorioPdf(@RequestParam(required = false) String keyword, HttpServletResponse response) throws IOException {
        // Busca a lista completa de produtos
        List<Produto> produtos = produtoService.listarTodosParaRelatorio(keyword);

        // Prepara as variáveis para o template
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("produtos", produtos);
        variaveis.put("dataGeracao", LocalDateTime.now());
        variaveis.put("totalProdutos", produtos.size());

        // Gera o PDF
        byte[] pdfBytes = pdfService.gerarPdfDeHtml("produto-relatorio.html", variaveis);

        // Envia o PDF para o navegador
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_produtos.pdf");
        response.getOutputStream().write(pdfBytes);
    }
}