package com.foody.delivery.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
		@NotBlank(message = "Nome do item é obrigatório")
		String name,

		@NotNull(message = "Quantidade é obrigatória")
		@Min(value = 1, message = "Quantidade deve ser no mínimo 1")
		Integer quantity,

		@NotNull(message = "Preço unitário é obrigatório")
		@Positive(message = "Preço unitário deve ser maior que zero")
		@Digits(integer = 8, fraction = 2, message = "Preço unitário deve ter no máximo 2 casas decimais")
		BigDecimal unitPrice) {

	public OrderItemRequest {
		name = InputNormalizer.trim(name);
	}

}
