package com.example.cego_webapp.controllers;

import com.example.cego_webapp.services.PdfService;
import com.example.cego_webapp.services.RelatorioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Import necessário
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import necessário

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;
    @Autowired
    private PdfService pdfService;

    @GetMapping("/faturamento")
    public String formFaturamento(Model model) {
        model.addAttribute("activePage", "relatorios"); // Para marcar no sidebar
        return "relatorios/faturamento-filtro";
    }

    @GetMapping("/faturamento/pdf")
    public void gerarRelatorioFaturamento(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                          HttpServletResponse response, RedirectAttributes redirectAttributes) throws IOException { // Adicionado RedirectAttributes

        if (inicio == null) {
            inicio = LocalDate.now().withDayOfMonth(1);
        }
        if (fim == null) {
            fim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        try {
            Map<String, Object> dados = relatorioService.gerarDadosFaturamento(inicio, fim);
            dados.put("dataGeracao", LocalDateTime.now());

            byte[] pdfBytes = pdfService.gerarPdfDeHtml("faturamento-relatorio.html", dados);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=relatorio_faturamento_" + inicio + "_a_" + fim + ".pdf"); // 'inline' abre no navegador
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);

        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório de faturamento: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar o relatório PDF.");
        }
    }

    @GetMapping("/fluxo-caixa")
    public String formFluxoCaixa(Model model) {
        model.addAttribute("activePage", "relatorios-fluxo-caixa"); // Para o sidebar
        return "relatorios/fluxo-caixa-filtro";
    }

    @GetMapping("/fluxo-caixa/pdf")
    public void gerarRelatorioFluxoCaixa(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                         HttpServletResponse response) throws IOException {

        try {
            Map<String, Object> dados = relatorioService.gerarDadosFluxoCaixa(inicio, fim);
            dados.put("dataGeracao", LocalDateTime.now());

            byte[] pdfBytes = pdfService.gerarPdfDeHtml("fluxo-caixa-relatorio.html", dados);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=relatorio_fluxo_caixa.pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório de fluxo de caixa: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar o relatório PDF.");
        }
    }

    @GetMapping("/desempenho-servicos")
    public String formDesempenhoServicos(Model model) {
        model.addAttribute("activePage", "relatorios-desempenho-servicos");
        return "relatorios/desempenho-servicos-filtro";
    }

    @GetMapping("/desempenho-servicos/pdf")
    public void gerarRelatorioDesempenhoServicos(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                                 HttpServletResponse response) throws IOException {

        try {
            Map<String, Object> dados = relatorioService.gerarDadosDesempenhoServicos(inicio, fim);
            dados.put("dataGeracao", LocalDateTime.now());

            byte[] pdfBytes = pdfService.gerarPdfDeHtml("desempenho-servicos-relatorio.html", dados);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=relatorio_desempenho_servicos.pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório de desempenho de serviços: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar o relatório PDF.");
        }
    }
}