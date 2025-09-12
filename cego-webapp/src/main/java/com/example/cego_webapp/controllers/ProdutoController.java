package com.example.cego_webapp.controllers;

import com.example.cego_webapp.dto.ProdutoDTO;
import com.example.cego_webapp.models.Produto;
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
        Sort.Direction sortDir = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

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
        ProdutoDTO produtoDTO = new ProdutoDTO();
        model.addAttribute("produtoDTO", produtoDTO);
        model.addAttribute("activePage", "produtos");
        return "produtos/create";
    }

    @PostMapping("/create")
    public String createProduto(@Valid @ModelAttribute ProdutoDTO produtoDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "produtos");
            return "produtos/create";
        }

        Produto produto = new Produto();
        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());
        produtoRepository.save(produto);

        return "redirect:/produtos/";
    }

    @GetMapping("/edit")
    public String editProduto(@RequestParam Integer id, Model model) {
        Produto produto = produtoRepository.findById(id).orElse(null);
        if (produto == null) {
            return "redirect:/produtos";
        }

        ProdutoDTO produtoDTO = new ProdutoDTO();
        produtoDTO.setNome(produto.getNome());
        produtoDTO.setDescricao(produto.getDescricao());
        produtoDTO.setPreco(produto.getPreco());
        produtoDTO.setEstoque(produto.getEstoque());

        model.addAttribute("produto", produto);
        model.addAttribute("produtoDTO", produtoDTO);
        model.addAttribute("activePage", "produtos");

        return "produtos/edit";
    }

    @PostMapping("/edit")
    public String editProduto(Model model, @RequestParam Integer id, @Valid @ModelAttribute ProdutoDTO produtoDTO, BindingResult bindingResult) {
        Produto produto = produtoRepository.findById(id).orElse(null);
        if (produto == null) {
            return "redirect:/produtos";
        }

        model.addAttribute("produto", produto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "produtos");
            return "produtos/edit";
        }

        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());
        produtoRepository.save(produto);

        return "redirect:/produtos";
    }

    @GetMapping("/delete")
    public String deleteProduto(@RequestParam Integer id) {
        produtoRepository.deleteById(id);
        return "redirect:/produtos";
    }
}