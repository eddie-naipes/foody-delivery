package com.foody.delivery.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
		@NotBlank(message = "Nome do cliente é obrigatório")
		String customerName,

		@NotBlank(message = "Telefone do cliente é obrigatório")
		String customerPhone,

		@NotNull(message = "Endereço de entrega é obrigatório")
		@Valid
		AddressRequest deliveryAddress,

		@NotEmpty(message = "O pedido deve ter ao menos um item")
		List<@Valid OrderItemRequest> items) {

	public CreateOrderRequest {
		customerName = InputNormalizer.trim(customerName);
		customerPhone = InputNormalizer.trim(customerPhone);
	}

}
