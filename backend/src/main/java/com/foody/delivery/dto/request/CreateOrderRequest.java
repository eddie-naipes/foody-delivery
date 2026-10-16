package com.foody.delivery.dto.request;

import java.util.List;

import com.foody.delivery.util.Messages;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
		@NotBlank(message = Messages.CUSTOMER_NAME_REQUIRED)
		String customerName,

		@NotBlank(message = Messages.CUSTOMER_PHONE_REQUIRED)
		String customerPhone,

		@NotNull(message = Messages.DELIVERY_ADDRESS_REQUIRED)
		@Valid
		AddressRequest deliveryAddress,

		@NotEmpty(message = Messages.ITEMS_REQUIRED)
		List<@NotNull(message = Messages.ITEM_REQUIRED) @Valid OrderItemRequest> items) {

	public CreateOrderRequest {
		customerName = InputNormalizer.trim(customerName);
		customerPhone = InputNormalizer.trim(customerPhone);
	}

}
