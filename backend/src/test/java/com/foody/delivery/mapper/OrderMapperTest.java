package com.foody.delivery.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.foody.delivery.dto.request.AddressRequest;
import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.request.OrderItemRequest;
import com.foody.delivery.dto.response.AddressResponse;
import com.foody.delivery.dto.response.OrderItemResponse;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderItem;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;

class OrderMapperTest {

	private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

	private final User user = new User("Operador", "op@foody.com", "hash");

	@Test
	void toResponseMapsAllFields() {
		Order order = new Order("Maria Silva", "11999998888",
				new Address("Av. Paulista", "1000", "Apto 12", "Bela Vista", "São Paulo", "01310100"), user);
		order.addItem(new OrderItem("Pizza", 2, new BigDecimal("45.00")));
		order.addItem(new OrderItem("Refrigerante", 1, new BigDecimal("9.90")));

		OrderResponse response = mapper.toResponse(order);

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

	@Test
	void toEntityBuildsOrderWithAddressItemsAndCreator() {
		CreateOrderRequest request = new CreateOrderRequest("Maria Silva", "11999998888",
				new AddressRequest("Av. Paulista", "1000", "Apto 12", "Bela Vista", "São Paulo", "01310-100"),
				List.of(new OrderItemRequest("Pizza", 2, new BigDecimal("45.00")),
						new OrderItemRequest("Refrigerante", 1, new BigDecimal("9.90"))));

		Order order = mapper.toEntity(request, user);

		assertThat(order.getCustomerName()).isEqualTo("Maria Silva");
		assertThat(order.getCustomerPhone()).isEqualTo("11999998888");
		assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEBIDO);
		assertThat(order.getCreatedBy()).isSameAs(user);
		assertThat(order.getDeliveryAddress().getZipCode()).isEqualTo("01310100");
		assertThat(order.getDeliveryAddress().getComplement()).isEqualTo("Apto 12");
		assertThat(order.getItems()).extracting(OrderItem::getName).containsExactly("Pizza", "Refrigerante");
		assertThat(order.getItems()).allSatisfy(item -> assertThat(item.getOrder()).isSameAs(order));
		assertThat(order.getTotalAmount()).isEqualByComparingTo("99.90");
	}

}
