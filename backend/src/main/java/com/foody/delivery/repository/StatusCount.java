package com.foody.delivery.repository;

import com.foody.delivery.model.OrderStatus;

/** Linha da contagem agrupada por status (só aparecem status com ao menos um pedido). */
public record StatusCount(OrderStatus status, Long total) {
}
