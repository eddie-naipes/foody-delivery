package com.foody.delivery.validation;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxBytesValidator implements ConstraintValidator<MaxBytes, String> {

	private int maxBytes;

	@Override
	public void initialize(MaxBytes annotation) {
		this.maxBytes = annotation.value();
	}

	/** Nulo é responsabilidade do {@code @NotBlank}. */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value == null || value.getBytes(StandardCharsets.UTF_8).length <= maxBytes;
	}

}
