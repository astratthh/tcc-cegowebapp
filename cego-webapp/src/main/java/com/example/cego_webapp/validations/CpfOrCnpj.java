package com.example.cego_webapp.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CpfOrCnpjValidator.class)
@Documented
public @interface CpfOrCnpj {
    String message() default "Documento Inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
