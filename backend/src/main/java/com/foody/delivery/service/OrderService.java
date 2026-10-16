package com.foody.delivery.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.exception.ResourceNotFoundException;
import com.foody.delivery.mapper.OrderMapper;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;
import com.foody.delivery.repository.OrderRepository;
import com.foody.delivery.util.Messages;

import lombok.RequiredArgsConstructor;

/**
 * Orquestra os casos de uso de pedidos. As regras de negócio (transição de status,
 * total) ficam nas entidades; aqui só se busca, delega e converte para DTO dentro da
 * transação.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;

	private final OrderMapper orderMapper;

	@Transactional
	public OrderResponse create(CreateOrderRequest request, User createdBy) {
		Order order = orderRepository.save(orderMapper.toEntity(request, createdBy));
		return orderMapper.toResponse(order);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> findAll() {
		return orderMapper.toResponses(orderRepository.findAllByOrderByCreatedAtDescIdDesc());
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(Long id) {
		return orderMapper.toResponse(getOrder(id));
	}

	@Transactional
	public OrderResponse updateStatus(Long id, OrderStatus status) {
		Order order = getOrder(id);
		order.changeStatus(status);
		// flush antecipado para que o updatedAt da resposta já reflita a alteração
		orderRepository.flush();
		return orderMapper.toResponse(order);
	}

	private Order getOrder(Long id) {
		return orderRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(Messages.ORDER_NOT_FOUND.formatted(id)));
	}

}
