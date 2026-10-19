package com.foody.delivery.dto.response;

import java.util.Map;

import com.foody.delivery.model.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/** Quantos pedidos há em cada status: a fila de relance, sem precisar carregar a lista inteira. */
public record StatusCountResponse(@Schema(example = "7") long total,
		@Schema(description = "Todos os status aparecem, inclusive os com zero",
				example = "{\"RECEBIDO\": 2, \"EM_PREPARO\": 1, \"SAIU_PARA_ENTREGA\": 1, \"ENTREGUE\": 2, \"CANCELADO\": 1}")
		Map<OrderStatus, Long> byStatus) {
}
