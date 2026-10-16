package com.foody.delivery.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderStatus;

public record OrderResponse(Long id, String customerName, String customerPhone, AddressResponse deliveryAddress,
		List<OrderItemResponse> items, BigDecimal totalAmount, OrderStatus status, CreatedBy createdBy,
		Instant createdAt, Instant updatedAt) {

	/** Apenas id e nome de quem criou o pedido — nunca o usuário completo. */
	public record CreatedBy(Long id, String name) {
	}

	public static OrderResponse from(Order order) {
		return new OrderResponse(order.getId(), order.getCustomerName(), order.getCustomerPhone(),
				AddressResponse.from(order.getDeliveryAddress()),
				order.getItems().stream().map(OrderItemResponse::from).toList(), order.getTotalAmount(),
				order.getStatus(), new CreatedBy(order.getCreatedBy().getId(), order.getCreatedBy().getName()),
				order.getCreatedAt(), order.getUpdatedAt());
	}

}
