package com.example.cego_webapp.services;

import com.example.cego_webapp.dto.FluxoCaixaDTO;
import com.example.cego_webapp.dto.ServicoDesempenhoDTO;
import com.example.cego_webapp.models.ContaPagar;
import com.example.cego_webapp.models.ContaReceber;
import com.example.cego_webapp.repositories.ContaPagarRepository;
import com.example.cego_webapp.repositories.ContaReceberRepository;
import com.example.cego_webapp.repositories.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class RelatorioService {

    @Autowired
    private ContaPagarRepository contaPagarRepository;
    @Autowired
    private ContaReceberRepository contaReceberRepository;
    @Autowired
    private ServicoRepository servicoRepository;

    public Map<String, Object> gerarDadosFaturamento(LocalDate inicio, LocalDate fim) {
        // Validação básica das datas
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Período inválido. A data de início deve ser anterior ou igual à data de fim.");
        }

        List<ContaReceber> vendasPagas = contaReceberRepository.findVendasRecebidasPorPeriodo(inicio, fim);
        List<ContaReceber> osPagas = contaReceberRepository.findOsRecebidasPorPeriodo(inicio, fim);

        BigDecimal totalVendas = vendasPagas.stream()
                .map(ContaReceber::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOS = osPagas.stream()
                .map(ContaReceber::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faturamentoTotal = totalVendas.add(totalOS);

        Map<String, Object> dados = new HashMap<>();
        dados.put("vendasPagas", vendasPagas);
        dados.put("osPagas", osPagas);
        dados.put("totalVendas", totalVendas);
        dados.put("totalOS", totalOS);
        dados.put("faturamentoTotal", faturamentoTotal);
        dados.put("dataInicio", inicio);
        dados.put("dataFim", fim);

        return dados;
    }

    @Transactional(readOnly = true) // Boa prática para relatórios
    public Map<String, Object> gerarDadosFluxoCaixa(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Período inválido.");
        }

        // 1. Buscar as listas detalhadas
        List<ContaReceber> entradasVendas = contaReceberRepository.findVendasRecebidasPorPeriodo(inicio, fim);
        List<ContaReceber> entradasOS = contaReceberRepository.findOsRecebidasPorPeriodo(inicio, fim);
        List<ContaPagar> saidas = contaPagarRepository.findPagasPorPeriodo(inicio, fim);

        // 2. Combina as listas de entrada em uma só e ordena por data
        List<ContaReceber> todasEntradas = new ArrayList<>(entradasVendas);
        todasEntradas.addAll(entradasOS);
        todasEntradas.sort(Comparator.comparing(ContaReceber::getDataRecebimento));

        // 3. Calcula os totais a partir das listas detalhadas
        BigDecimal totalEntradas = todasEntradas.stream()
                .map(ContaReceber::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = saidas.stream()
                .map(ContaPagar::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fluxoLiquido = totalEntradas.subtract(totalSaidas);

        // 4. Monta o mapa de dados
        Map<String, Object> dados = new HashMap<>();
        dados.put("entradas", todasEntradas); // Passa a lista detalhada
        dados.put("saidas", saidas);       // Passa a lista detalhada
        dados.put("totalEntradas", totalEntradas);
        dados.put("totalSaidas", totalSaidas);
        dados.put("fluxoCaixaLiquido", fluxoLiquido);
        dados.put("dataInicio", inicio);
        dados.put("dataFim", fim);

        return dados;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> gerarDadosDesempenhoServicos(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Período inválido.");
        }

        List<ServicoDesempenhoDTO> servicos = servicoRepository.findDesempenhoServicosPorPeriodo(inicio, fim);

        // Calcula o faturamento total de todos os serviços no período
        BigDecimal faturamentoTotalServicos = servicos.stream()
                .map(ServicoDesempenhoDTO::getFaturamentoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> dados = new HashMap<>();
        dados.put("servicos", servicos);
        dados.put("faturamentoTotalServicos", faturamentoTotalServicos);
        dados.put("dataInicio", inicio);
        dados.put("dataFim", fim);

        return dados;
    }
}