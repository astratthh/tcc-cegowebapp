package com.example.cego_webapp.controllers;

import com.example.cego_webapp.repositories.ClienteRepository;
import com.example.cego_webapp.repositories.FuncionarioRepository;
import com.example.cego_webapp.repositories.ProdutoRepository;
import com.example.cego_webapp.repositories.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private ServicoRepository servicoRepository;

    @GetMapping
    public String home(Model model) {
        // Busca as contagens totais para os cards
        model.addAttribute("totalClientes", clienteRepository.count());
        model.addAttribute("totalProdutos", produtoRepository.count());
        model.addAttribute("totalFuncionarios", funcionarioRepository.count());
        model.addAttribute("totalServicos", servicoRepository.count());

        // Busca as listas dos últimos 5 adicionados
        model.addAttribute("ultimosClientes", clienteRepository.findTop5ByOrderByIdDesc());
        model.addAttribute("ultimosServicos", servicoRepository.findTop5ByOrderByIdDesc());

        // Define a página ativa para a sidebar
        model.addAttribute("activePage", "home");

        return "index";
    }
}