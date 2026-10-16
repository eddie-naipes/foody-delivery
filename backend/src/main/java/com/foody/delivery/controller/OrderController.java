package com.foody.delivery.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.request.UpdateStatusRequest;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.model.User;
import com.foody.delivery.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
			@AuthenticationPrincipal User user) {
		OrderResponse created = orderService.create(request, user);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(created.id())
			.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	public List<OrderResponse> findAll() {
		return orderService.findAll();
	}

	@GetMapping("/{id}")
	public OrderResponse findById(@PathVariable Long id) {
		return orderService.findById(id);
	}

	@PatchMapping("/{id}/status")
	public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
		return orderService.updateStatus(id, request.status());
	}

}
