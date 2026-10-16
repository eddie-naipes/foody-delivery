package com.foody.delivery.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(Long id, String name, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}
