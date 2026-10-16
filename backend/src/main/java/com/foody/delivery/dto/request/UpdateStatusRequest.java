package com.foody.delivery.dto.request;

import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.util.Messages;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull(message = Messages.STATUS_REQUIRED) OrderStatus status) {
}
