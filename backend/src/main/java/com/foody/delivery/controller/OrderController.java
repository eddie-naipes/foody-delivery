package com.foody.delivery.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.foody.delivery.config.OpenApiConfig;
import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.request.UpdateStatusRequest;
import com.foody.delivery.dto.response.ApiError;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.dto.response.PageResponse;
import com.foody.delivery.dto.response.StatusCountResponse;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;
import com.foody.delivery.service.OrderService;
import com.foody.delivery.util.Messages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos", description = "Criação, consulta e acompanhamento do status dos pedidos")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@ApiResponse(responseCode = "401", description = "Sem token ou token inválido/expirado",
		content = @Content(schema = @Schema(implementation = ApiError.class)))
public class OrderController {

	static final int MAX_PAGE_SIZE = 100;

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@Operation(summary = "Cria um pedido",
			description = "O pedido nasce como RECEBIDO, em nome do usuário logado. CEP e telefone aceitam máscara.")
	@ApiResponse(responseCode = "201", description = "Pedido criado; o header Location aponta para ele")
	@ApiResponse(responseCode = "400", description = "Dados inválidos, com o erro de cada campo",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal User user) {
		OrderResponse created = orderService.create(request, user);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(created.id())
			.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	@Operation(summary = "Lista os pedidos, paginados",
			description = "Mais recentes primeiro. Filtra por status quando informado.")
	@ApiResponse(responseCode = "200", description = "Página de pedidos")
	@ApiResponse(responseCode = "400", description = "Status desconhecido ou página/tamanho fora dos limites",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	public PageResponse<OrderResponse> findPage(
			@Parameter(description = "Mostra só os pedidos neste status") @RequestParam(required = false) OrderStatus status,
			@Parameter(description = "Número da página, começando em 0") @RequestParam(defaultValue = "0")
			@Min(value = 0, message = Messages.PAGE_MIN) int page,
			@Parameter(description = "Pedidos por página (1 a 100)") @RequestParam(defaultValue = "20")
			@Min(value = 1, message = Messages.PAGE_SIZE_RANGE) @Max(value = MAX_PAGE_SIZE, message = Messages.PAGE_SIZE_RANGE) int size) {
		return orderService.findPage(status, page, size);
	}

	@GetMapping("/status-count")
	@Operation(summary = "Conta os pedidos por status",
			description = "Todos os status aparecem, inclusive os com zero. Alimenta os filtros da lista.")
	public StatusCountResponse countByStatus() {
		return orderService.countByStatus();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um pedido", description = "Inclui itens, total e a linha do tempo de status.")
	@ApiResponse(responseCode = "200", description = "Pedido encontrado")
	@ApiResponse(responseCode = "404", description = "Pedido inexistente",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	public OrderResponse findById(@PathVariable Long id) {
		return orderService.findById(id);
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "Altera o status do pedido",
			description = "Fluxo: RECEBIDO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE. Qualquer etapa não final pode ir "
					+ "para CANCELADO. A mudança entra na linha do tempo com o usuário logado.")
	@ApiResponse(responseCode = "200", description = "Status alterado")
	@ApiResponse(responseCode = "400", description = "Status ausente ou desconhecido",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	@ApiResponse(responseCode = "404", description = "Pedido inexistente",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	@ApiResponse(responseCode = "422", description = "Transição não permitida (ex.: RECEBIDO → ENTREGUE)",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal User user) {
		return orderService.updateStatus(id, request.status(), user);
	}

}
