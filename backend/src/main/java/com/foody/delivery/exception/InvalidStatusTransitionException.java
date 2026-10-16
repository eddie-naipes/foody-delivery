package com.foody.delivery.exception;

import org.springframework.http.HttpStatus;

import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.util.Messages;

public class InvalidStatusTransitionException extends ApiException {

	public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
		super(HttpStatus.UNPROCESSABLE_CONTENT, Messages.INVALID_STATUS_TRANSITION.formatted(from, to));
	}

}
