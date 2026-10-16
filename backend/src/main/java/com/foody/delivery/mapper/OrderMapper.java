package com.foody.delivery.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.foody.delivery.dto.request.AddressRequest;
import com.foody.delivery.dto.request.CreateOrderRequest;
import com.foody.delivery.dto.request.OrderItemRequest;
import com.foody.delivery.dto.response.OrderResponse;
import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderItem;
import com.foody.delivery.model.User;

/**
 * Conversões entre pedidos e seus DTOs. A implementação é gerada pelo MapStruct em
 * tempo de compilação; {@code unmappedTargetPolicy = ERROR} faz um campo esquecido
 * quebrar o build em vez de virar {@code null} silenciosamente.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

	OrderResponse toResponse(Order order);

	List<OrderResponse> toResponses(List<Order> orders);

	OrderResponse.CreatedBy toCreatedBy(User user);

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
