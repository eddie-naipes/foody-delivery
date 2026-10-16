package com.foody.delivery.dto.request;

import com.foody.delivery.model.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull(message = "Status é obrigatório") OrderStatus status) {
}
