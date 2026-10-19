package com.foody.delivery.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.foody.delivery.dto.request.AddressRequest;
import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.request.OrderItemRequest;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.dto.response.StatusChangeResponse;
import com.foody.delivery.dto.response.UserSummaryResponse;
import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderItem;
import com.foody.delivery.model.OrderStatusChange;
import com.foody.delivery.model.User;

/**
 * Conversões entre pedidos e seus DTOs. A implementação é gerada pelo MapStruct em
 * tempo de compilação; {@code unmappedTargetPolicy = ERROR} faz um campo esquecido
 * quebrar o build em vez de virar {@code null} silenciosamente.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

	OrderResponse toResponse(Order order);

	StatusChangeResponse toResponse(OrderStatusChange change);

	UserSummaryResponse toUserSummary(User user);

	/**
	 * Valores monetários sempre com 2 casas: o SQLite devolve 45.00 como 45, e a API
	 * deve responder de forma consistente.
	 */
	default BigDecimal toMoney(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	Address toEntity(AddressRequest request);

	OrderItem toEntity(OrderItemRequest request);

	/**
	 * Monta um pedido novo. Escrito à mão porque o pedido precisa do usuário logado e
	 * os itens devem entrar por {@link Order#addItem}, que liga os dois lados da relação.
	 */
	default Order toEntity(CreateOrderRequest request, User createdBy) {
		Order order = new Order(request.customerName(), request.customerPhone(),
				toEntity(request.deliveryAddress()), createdBy);
		request.items().forEach(item -> order.addItem(toEntity(item)));
		return order;
	}

}
