package com.foody.delivery.dto.request;

import java.math.BigDecimal;

import com.foody.delivery.util.Messages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
		@Schema(example = "Pizza Margherita")
		@NotBlank(message = Messages.ITEM_NAME_REQUIRED)
		String name,

		@Schema(example = "2")
		@NotNull(message = Messages.QUANTITY_REQUIRED)
		@Min(value = 1, message = Messages.QUANTITY_MIN)
		Integer quantity,

		@Schema(example = "45.00", description = "Preço unitário no momento do pedido, até 2 casas decimais")
		@NotNull(message = Messages.UNIT_PRICE_REQUIRED)
		@Positive(message = Messages.UNIT_PRICE_POSITIVE)
		@Digits(integer = 8, fraction = 2, message = Messages.UNIT_PRICE_DECIMALS)
		BigDecimal unitPrice) {

	public OrderItemRequest {
		name = InputNormalizer.trim(name);
	}

}
