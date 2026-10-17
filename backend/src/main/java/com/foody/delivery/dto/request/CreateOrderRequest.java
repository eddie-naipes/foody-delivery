package com.foody.delivery.dto.request;

import java.util.List;

import com.foody.delivery.util.Messages;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateOrderRequest(
		@NotBlank(message = Messages.CUSTOMER_NAME_REQUIRED)
		String customerName,

		@NotBlank(message = Messages.CUSTOMER_PHONE_REQUIRED)
		@Pattern(regexp = "\\d{10,11}", message = Messages.CUSTOMER_PHONE_INVALID)
		String customerPhone,

		@NotNull(message = Messages.DELIVERY_ADDRESS_REQUIRED)
		@Valid
		AddressRequest deliveryAddress,

		@NotEmpty(message = Messages.ITEMS_REQUIRED)
		List<@NotNull(message = Messages.ITEM_REQUIRED) @Valid OrderItemRequest> items) {

	public CreateOrderRequest {
		customerName = InputNormalizer.trim(customerName);
		customerPhone = InputNormalizer.phone(customerPhone);
	}

}
