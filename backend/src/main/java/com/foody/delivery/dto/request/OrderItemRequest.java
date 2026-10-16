package com.foody.delivery.dto.request;

import java.math.BigDecimal;

import com.foody.delivery.util.Messages;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
		@NotBlank(message = Messages.ITEM_NAME_REQUIRED)
		String name,

		@NotNull(message = Messages.QUANTITY_REQUIRED)
		@Min(value = 1, message = Messages.QUANTITY_MIN)
		Integer quantity,

		@NotNull(message = Messages.UNIT_PRICE_REQUIRED)
		@Positive(message = Messages.UNIT_PRICE_POSITIVE)
		@Digits(integer = 8, fraction = 2, message = Messages.UNIT_PRICE_DECIMALS)
		BigDecimal unitPrice) {

	public OrderItemRequest {
		name = InputNormalizer.trim(name);
	}

}
