package com.foody.delivery.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Limita o tamanho do texto em bytes UTF-8 (não em caracteres). Necessário para a
 * senha: o BCrypt só aceita até 72 bytes, e um caractere acentuado ocupa 2.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxBytesValidator.class)
public @interface MaxBytes {

	int value();

	String message();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
