package com.foody.delivery.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class OrderItemTest {

	@Test
	void subtotalIsQuantityTimesUnitPrice() {
		OrderItem item = new OrderItem("Pizza", 2, new BigDecimal("45.00"));

		assertThat(item.getSubtotal()).isEqualByComparingTo("90.00");
	}

	@Test
	void exposesConstructorValues() {
		OrderItem item = new OrderItem("Refrigerante", 3, new BigDecimal("7.50"));

		assertThat(item.getName()).isEqualTo("Refrigerante");
		assertThat(item.getQuantity()).isEqualTo(3);
		assertThat(item.getUnitPrice()).isEqualByComparingTo("7.50");
		assertThat(item.getOrder()).isNull();
	}

}
