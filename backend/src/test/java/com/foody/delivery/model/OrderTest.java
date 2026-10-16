package com.foody.delivery.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.foody.delivery.exception.InvalidStatusTransitionException;

class OrderTest {

	private final User user = new User("Operador", "op@foody.com", "hash");

	private final Address address = new Address("Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "01310100");

	private Order newOrder() {
		return new Order("Maria Silva", "11999998888", address, user);
	}

	@Test
	void newOrderStartsAsRecebido() {
		assertThat(newOrder().getStatus()).isEqualTo(OrderStatus.RECEBIDO);
	}

	@Test
	void exposesConstructorValues() {
		Order order = newOrder();

		assertThat(order.getCustomerName()).isEqualTo("Maria Silva");
		assertThat(order.getCustomerPhone()).isEqualTo("11999998888");
		assertThat(order.getDeliveryAddress()).isSameAs(address);
		assertThat(order.getCreatedBy()).isSameAs(user);
	}

	@Test
	void totalAmountSumsSubtotals() {
		Order order = newOrder();
		order.addItem(new OrderItem("Pizza", 2, new BigDecimal("45.00")));
		order.addItem(new OrderItem("Refrigerante", 1, new BigDecimal("9.90")));

		assertThat(order.getTotalAmount()).isEqualByComparingTo("99.90");
	}

	@Test
	void totalAmountIsZeroWithoutItems() {
		assertThat(newOrder().getTotalAmount()).isEqualByComparingTo("0");
	}

	@Test
	void addItemLinksBothSides() {
		Order order = newOrder();
		OrderItem item = new OrderItem("Pizza", 1, new BigDecimal("45.00"));

		order.addItem(item);

		assertThat(order.getItems()).containsExactly(item);
		assertThat(item.getOrder()).isSameAs(order);
	}

	@Test
	void itemsListIsUnmodifiable() {
		Order order = newOrder();
		OrderItem item = new OrderItem("Pizza", 1, new BigDecimal("45.00"));

		assertThatThrownBy(() -> order.getItems().add(item))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void changeStatusUpdatesValidTransition() {
		Order order = newOrder();

		order.changeStatus(OrderStatus.EM_PREPARO);

		assertThat(order.getStatus()).isEqualTo(OrderStatus.EM_PREPARO);
	}

	@Test
	void changeStatusRejectsInvalidTransition() {
		Order order = newOrder();
		order.changeStatus(OrderStatus.EM_PREPARO);
		order.changeStatus(OrderStatus.SAIU_PARA_ENTREGA);
		order.changeStatus(OrderStatus.ENTREGUE);

		assertThatThrownBy(() -> order.changeStatus(OrderStatus.EM_PREPARO))
			.isInstanceOf(InvalidStatusTransitionException.class)
			.hasMessage("Transição de ENTREGUE para EM_PREPARO não permitida");
		assertThat(order.getStatus()).isEqualTo(OrderStatus.ENTREGUE);
	}

}
