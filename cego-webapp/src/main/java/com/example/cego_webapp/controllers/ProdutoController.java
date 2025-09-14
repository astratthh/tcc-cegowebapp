package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ProdutoDTO;
import com.example.cego_webapp.models.Produto;
import com.example.cego_webapp.repositories.ProdutoRepository;
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

@Controller
@RequestMapping("/produtos")
public class ProdutoController {
    @Autowired
    private ProdutoRepository produtoRepository;

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

        Page<Produto> produtosPage;
        if (keyword != null && !keyword.isEmpty()) {
            produtosPage = produtoRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            produtosPage = produtoRepository.findAll(pageable);
        }

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
    public String createProduto(Model model) {
        if (!model.containsAttribute("produtoDTO")) {
            model.addAttribute("produtoDTO", new ProdutoDTO());
        }
        model.addAttribute("activePage", "produtos");
        return "produtos/create";
    }

    @PostMapping("/create")
    public String createProduto(@Valid @ModelAttribute("produtoDTO") ProdutoDTO produtoDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.produtoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("produtoDTO", produtoDTO);
            return "redirect:/produtos/create";
        }

        Produto produto = new Produto();
        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());
        produtoRepository.save(produto);

        redirectAttributes.addFlashAttribute("successMessage", "Produto cadastrado com sucesso!");
        return "redirect:/produtos";
    }

    @GetMapping("/edit")
    public String editProduto(@RequestParam Integer id, Model model) {
        Produto produto = produtoRepository.findById(id).orElse(null);
        if (produto == null) {
            return "redirect:/produtos";
        }

        if (!model.containsAttribute("produtoDTO")) {
            ProdutoDTO produtoDTO = new ProdutoDTO();
            produtoDTO.setNome(produto.getNome());
            produtoDTO.setDescricao(produto.getDescricao());
            produtoDTO.setPreco(produto.getPreco());
            produtoDTO.setEstoque(produto.getEstoque());
            model.addAttribute("produtoDTO", produtoDTO);
        }

        model.addAttribute("produto", produto);
        model.addAttribute("activePage", "produtos");
        return "produtos/edit";
    }

    @PostMapping("/edit")
    public String editProduto(@RequestParam Integer id, @Valid @ModelAttribute("produtoDTO") ProdutoDTO produtoDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Produto produto = produtoRepository.findById(id).orElse(null);
        if (produto == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Produto não encontrado.");
            return "redirect:/produtos";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.produtoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("produtoDTO", produtoDTO);
            return "redirect:/produtos/edit?id=" + id;
        }

        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());
        produtoRepository.save(produto);

        redirectAttributes.addFlashAttribute("successMessage", "Produto atualizado com sucesso!");
        return "redirect:/produtos";
    }

    @GetMapping("/delete")
    public String deleteProduto(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            if (!produtoRepository.existsById(id)) {
                throw new Exception("Produto não encontrado.");
            }
            produtoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produto excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Não é possível excluir o produto, pois ele está associado a uma ou mais vendas.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir o produto: " + e.getMessage());
        }
        return "redirect:/produtos";
    }
}