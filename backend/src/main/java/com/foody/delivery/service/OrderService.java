package com.foody.delivery.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foody.delivery.dto.request.AddressRequest;
import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.exception.ResourceNotFoundException;
import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderItem;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;
import com.foody.delivery.repository.OrderRepository;

/**
 * Orquestra os casos de uso de pedidos. As regras de negócio (transição de status,
 * total) ficam nas entidades; aqui só se busca, delega e converte para DTO dentro da
 * transação.
 */
@Service
public class OrderService {

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Transactional
	public OrderResponse create(CreateOrderRequest request, User createdBy) {
		AddressRequest address = request.deliveryAddress();
		Order order = new Order(request.customerName(), request.customerPhone(),
				new Address(address.street(), address.number(), address.complement(), address.neighborhood(),
						address.city(), address.zipCode()),
				createdBy);
		request.items().forEach(item -> order.addItem(new OrderItem(item.name(), item.quantity(), item.unitPrice())));
		return OrderResponse.from(orderRepository.save(order));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> findAll() {
		return orderRepository.findAllByOrderByCreatedAtDescIdDesc().stream().map(OrderResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(Long id) {
		return OrderResponse.from(getOrder(id));
	}

	@Transactional
	public OrderResponse updateStatus(Long id, OrderStatus status) {
		Order order = getOrder(id);
		order.changeStatus(status);
		// flush antecipado para que o updatedAt da resposta já reflita a alteração
		orderRepository.flush();
		return OrderResponse.from(order);
	}

	private Order getOrder(Long id) {
		return orderRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Pedido " + id + " não encontrado"));
	}

}
