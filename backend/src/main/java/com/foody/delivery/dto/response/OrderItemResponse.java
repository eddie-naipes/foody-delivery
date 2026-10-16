package com.foody.delivery.dto.response;

import java.math.BigDecimal;

import com.foody.delivery.model.OrderItem;

public record OrderItemResponse(Long id, String name, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {

	public static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(item.getId(), item.getName(), item.getQuantity(), item.getUnitPrice(),
				item.getSubtotal());
	}

}
