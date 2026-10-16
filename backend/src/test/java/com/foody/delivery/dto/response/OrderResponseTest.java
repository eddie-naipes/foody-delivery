package com.foody.delivery.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderItem;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;

class OrderResponseTest {

	@Test
	void mapsAllFields() {
		User user = new User("Operador", "op@foody.com", "hash");
		Order order = new Order("Maria Silva", "11999998888",
				new Address("Av. Paulista", "1000", "Apto 12", "Bela Vista", "São Paulo", "01310100"), user);
		order.addItem(new OrderItem("Pizza", 2, new BigDecimal("45.00")));
		order.addItem(new OrderItem("Refrigerante", 1, new BigDecimal("9.90")));

		OrderResponse response = OrderResponse.from(order);

		assertThat(response.customerName()).isEqualTo("Maria Silva");
		assertThat(response.customerPhone()).isEqualTo("11999998888");
		assertThat(response.status()).isEqualTo(OrderStatus.RECEBIDO);
		assertThat(response.totalAmount()).isEqualByComparingTo("99.90");
		assertThat(response.createdBy().name()).isEqualTo("Operador");

		AddressResponse address = response.deliveryAddress();
		assertThat(address.street()).isEqualTo("Av. Paulista");
		assertThat(address.number()).isEqualTo("1000");
		assertThat(address.complement()).isEqualTo("Apto 12");
		assertThat(address.neighborhood()).isEqualTo("Bela Vista");
		assertThat(address.city()).isEqualTo("São Paulo");
		assertThat(address.zipCode()).isEqualTo("01310100");

		assertThat(response.items()).hasSize(2);
		OrderItemResponse pizza = response.items().get(0);
		assertThat(pizza.name()).isEqualTo("Pizza");
		assertThat(pizza.quantity()).isEqualTo(2);
		assertThat(pizza.unitPrice()).isEqualByComparingTo("45.00");
		assertThat(pizza.subtotal()).isEqualByComparingTo("90.00");
	}

}
