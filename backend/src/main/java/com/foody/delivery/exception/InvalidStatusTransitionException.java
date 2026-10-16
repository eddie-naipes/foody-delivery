package com.foody.delivery.exception;

import com.foody.delivery.model.OrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {

	public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
		super("Transição de " + from + " para " + to + " não permitida");
	}

}
