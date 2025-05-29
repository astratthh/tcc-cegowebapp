package com.example.cego_webapp.validations;

public class CPFValidator {
    public static boolean isValid(String cpf, Object o) {
        return new org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator()
                .isValid(cpf, null);
    }
}
