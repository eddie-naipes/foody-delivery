package com.foody.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foody.delivery.dto.request.AddressRequest;
import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.request.OrderItemRequest;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.exception.InvalidStatusTransitionException;
import com.foody.delivery.exception.ResourceNotFoundException;
import com.foody.delivery.mapper.OrderMapper;
import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;
import com.foody.delivery.repository.OrderRepository;
import com.foody.delivery.util.Messages;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Spy
	private OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

	@InjectMocks
	private OrderService orderService;

	private final User user = new User("Operador", "op@foody.com", "hash");

	private Order existingOrder() {
		return new Order("Maria Silva", "11999998888",
				new Address("Rua A", "1", null, "Centro", "São Paulo", "01001000"), user);
	}

	@Test
	void createPersistsOrderWithItemsAsRecebido() {
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
		CreateOrderRequest request = new CreateOrderRequest("Maria Silva", "11999998888",
				new AddressRequest("Av. Paulista", "1000", "Apto 12", "Bela Vista", "São Paulo", "01310-100"),
				List.of(new OrderItemRequest("Pizza", 2, new BigDecimal("45.00")),
						new OrderItemRequest("Refrigerante", 1, new BigDecimal("9.90"))));

		OrderResponse response = orderService.create(request, user);

		ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
		verify(orderRepository).save(saved.capture());
		Order order = saved.getValue();
		assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEBIDO);
		assertThat(order.getCreatedBy()).isSameAs(user);
		assertThat(order.getItems()).hasSize(2);
		assertThat(order.getDeliveryAddress().getZipCode()).isEqualTo("01310100");
		assertThat(order.getDeliveryAddress().getComplement()).isEqualTo("Apto 12");
		assertThat(response.totalAmount()).isEqualByComparingTo("99.90");
	}

	@Test
	void findAllReturnsOrdersFromRepositoryInOrder() {
		Order first = existingOrder();
		Order second = existingOrder();
		when(orderRepository.findAllNewestFirst()).thenReturn(List.of(second, first));

		assertThat(orderService.findAll()).hasSize(2);
	}

	@Test
	void findByIdReturnsOrder() {
		when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder()));

		assertThat(orderService.findById(1L).customerName()).isEqualTo("Maria Silva");
	}

	@Test
	void findByIdMissingThrowsNotFound() {
		when(orderRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.findById(99L))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage(Messages.ORDER_NOT_FOUND.formatted(99));
	}

	@Test
	void updateStatusAppliesValidTransition() {
		Order order = existingOrder();
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		OrderResponse response = orderService.updateStatus(1L, OrderStatus.EM_PREPARO);

		assertThat(response.status()).isEqualTo(OrderStatus.EM_PREPARO);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.EM_PREPARO);
		verify(orderRepository).flush();
	}

	@Test
	void updateStatusInvalidTransitionThrows() {
		when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder()));

		assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.ENTREGUE))
			.isInstanceOf(InvalidStatusTransitionException.class)
			.hasMessage(Messages.INVALID_STATUS_TRANSITION.formatted(OrderStatus.RECEBIDO, OrderStatus.ENTREGUE));
	}

	@Test
	void updateStatusMissingThrowsNotFound() {
		when(orderRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.updateStatus(99L, OrderStatus.EM_PREPARO))
			.isInstanceOf(ResourceNotFoundException.class);
	}

}
