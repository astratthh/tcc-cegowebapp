package com.example.cego_webapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] gerarPdfDeHtml(String templateNome, Map<String, Object> variaveis) {
        // Cria o contexto do Thymeleaf com as variáveis
        Context context = new Context();
        context.setVariables(variaveis);

        // Processa o template HTML para uma String
        String html = templateEngine.process("relatorios/" + templateNome, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Usa o Flying Saucer para renderizar o HTML em PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace(); // Loga o erro para debug
            throw new RuntimeException("Erro ao gerar o relatório PDF.", e);
        }
    }
}