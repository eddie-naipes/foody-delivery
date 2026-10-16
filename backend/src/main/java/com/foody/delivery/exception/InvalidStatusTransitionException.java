package com.foody.delivery.exception;

import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.util.Messages;

public class InvalidStatusTransitionException extends RuntimeException {

	public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
		super(Messages.INVALID_STATUS_TRANSITION.formatted(from, to));
	}

}
