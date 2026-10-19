package com.foody.delivery.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.foody.delivery.model.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderResponse(Long id, String customerName,
		@Schema(description = "Somente dígitos, com DDD", example = "11999998888") String customerPhone,
		AddressResponse deliveryAddress, List<OrderItemResponse> items,
		@Schema(description = "Soma dos subtotais", example = "99.90") BigDecimal totalAmount, OrderStatus status,
		UserSummaryResponse createdBy,
		@Schema(description = "Linha do tempo do pedido, da criação ao status atual") List<StatusChangeResponse> statusHistory,
		Instant createdAt, Instant updatedAt) {
}
