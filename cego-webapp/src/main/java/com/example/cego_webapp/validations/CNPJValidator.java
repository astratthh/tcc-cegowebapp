package com.example.cego_webapp.validations;

public class CNPJValidator {
    public static boolean isValid(String cnpj, Object o) {
        return new org.hibernate.validator.internal.constraintvalidators.hv.br.CNPJValidator()
                .isValid(cnpj, null);
    }
}
