package com.example.cego_webapp.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfOrCnpjValidator implements ConstraintValidator<CpfOrCnpj, String> {

    @Override
    public boolean isValid(String documento, ConstraintValidatorContext context) {
        if (documento == null || documento.isBlank()) {
            return false; // Campo obrigatório
        }

        // Remove formatação
        String documentoLimpo = documento.replaceAll("[^0-9]", "");

        // Validação de CPF
        if (documentoLimpo.length() == 11) {
            return isValidCPF(documentoLimpo);
        }

        // Validação de CNPJ
        if (documentoLimpo.length() == 14) {
            return isValidCNPJ(documentoLimpo);
        }

        return false;
    }

    private boolean isValidCPF(String cpf) {
        if (cpf.length() != 11 || cpf.matches(cpf.charAt(0) + "{11}")) return false;

        int[] weights = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        // Calcula primeiro dígito
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * weights[i + 1];
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit > 9) firstDigit = 0;

        // Calcula segundo dígito
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * weights[i];
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit > 9) secondDigit = 0;

        return (cpf.charAt(9) - '0' == firstDigit) &&
                (cpf.charAt(10) - '0' == secondDigit);
    }

    private boolean isValidCNPJ(String cnpj) {
        if (cnpj.length() != 14 || cnpj.matches(cnpj.charAt(0) + "{14}")) return false;

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        // Calcula primeiro dígito
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * weights1[i];
        }
        int firstDigit = sum % 11;
        firstDigit = firstDigit < 2 ? 0 : 11 - firstDigit;

        // Calcula segundo dígito
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * weights2[i];
        }
        int secondDigit = sum % 11;
        secondDigit = secondDigit < 2 ? 0 : 11 - secondDigit;

        return (cnpj.charAt(12) - '0' == firstDigit) &&
                (cnpj.charAt(13) - '0' == secondDigit);
    }
}