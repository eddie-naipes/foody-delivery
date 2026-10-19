package com.foody.delivery.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.dto.response.PageResponse;
import com.foody.delivery.dto.response.StatusCountResponse;
import com.foody.delivery.exception.ResourceNotFoundException;
import com.foody.delivery.mapper.OrderMapper;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;
import com.foody.delivery.repository.OrderRepository;
import com.foody.delivery.util.Messages;

import lombok.extern.slf4j.Slf4j;

/**
 * Orquestra os casos de uso de pedidos. As regras de negócio (transição de status,
 * total) ficam nas entidades; aqui só se busca, delega e converte para DTO dentro da
 * transação.
 */
@Slf4j
@Service
public class OrderService {

	private final OrderRepository orderRepository;

	private final OrderMapper orderMapper;

	public OrderService(OrderRepository orderRepository, OrderMapper orderMapper) {
		this.orderRepository = orderRepository;
		this.orderMapper = orderMapper;
	}

	@Transactional
	public OrderResponse create(CreateOrderRequest request, User createdBy) {
		Order order = orderRepository.save(orderMapper.toEntity(request, createdBy));
		log.info("Pedido criado: id={}, itens={}, criadoPor={}", order.getId(), order.getItems().size(),
				createdBy.getId());
		return orderMapper.toResponse(order);
	}

	/** Uma página da lista, mais recentes primeiro, opcionalmente só de um status. */
	@Transactional(readOnly = true)
	public PageResponse<OrderResponse> findPage(OrderStatus status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, OrderRepository.NEWEST_FIRST);
		Page<Order> orders = status == null ? orderRepository.findAll(pageable)
				: orderRepository.findByStatus(status, pageable);
		return PageResponse.from(orders.map(orderMapper::toResponse));
	}

	/** Contagem por status, com zero para os que não têm pedidos. */
	@Transactional(readOnly = true)
	public StatusCountResponse countByStatus() {
		Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
		Arrays.stream(OrderStatus.values()).forEach(status -> byStatus.put(status, 0L));
		orderRepository.countByStatus().forEach(count -> byStatus.put(count.status(), count.total()));
		long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
		return new StatusCountResponse(total, byStatus);
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(Long id) {
		return orderMapper.toResponse(getOrder(id));
	}

	@Transactional
	public OrderResponse updateStatus(Long id, OrderStatus status, User changedBy) {
		Order order = getOrder(id);
		OrderStatus previous = order.getStatus();
		order.changeStatus(status, changedBy);
		log.info("Status do pedido {} alterado: {} -> {}, por={}", id, previous, status, changedBy.getId());
		// flush antecipado para que o updatedAt e a nova etapa do histórico já venham na resposta
		orderRepository.flush();
		return orderMapper.toResponse(order);
	}

	private Order getOrder(Long id) {
		return orderRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(Messages.ORDER_NOT_FOUND.formatted(id)));
	}

}
