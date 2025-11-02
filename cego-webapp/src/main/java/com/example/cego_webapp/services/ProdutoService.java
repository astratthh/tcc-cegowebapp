// src/main/java/com/example/cego_webapp/services/ProdutoService.java
package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.ProdutoDTO;
import com.example.cego_webapp.models.Produto;
import com.example.cego_webapp.repositories.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Page<Produto> listarProdutos(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return produtoRepository.findByNomeContainingIgnoreCase(keyword, pageable);
        } else {
            return produtoRepository.findAll(pageable);
        }
    }

    public Produto buscarPorId(Integer id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto com ID " + id + " não encontrado."));
    }

    public List<Produto> listarTodosParaRelatorio(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return produtoRepository.findAllByNomeContainingIgnoreCase(keyword);
        } else {
            return produtoRepository.findAll();
        }
    }

    @Transactional
    public Produto criarProduto(ProdutoDTO produtoDTO) {
        Produto produto = new Produto();
        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarProduto(Integer id, ProdutoDTO produtoDTO) {
        Produto produto = buscarPorId(id);

        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());
        return produtoRepository.save(produto);
    }

    @Transactional
    public void deletarProduto(Integer id) {
        if (!produtoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado.");
        }
        produtoRepository.deleteById(id);
    }
}