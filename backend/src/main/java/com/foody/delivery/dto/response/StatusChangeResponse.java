package com.foody.delivery.dto.response;

import java.time.Instant;

import com.foody.delivery.model.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/** Uma etapa da linha do tempo do pedido. */
public record StatusChangeResponse(OrderStatus status,
		@Schema(description = "Quem fez a mudança; nulo em registros anteriores ao histórico", nullable = true)
		UserSummaryResponse changedBy,
		Instant changedAt) {
}
