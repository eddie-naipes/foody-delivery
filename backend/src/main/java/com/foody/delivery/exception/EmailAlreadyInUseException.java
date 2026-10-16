package com.foody.delivery.exception;

import org.springframework.http.HttpStatus;

import com.foody.delivery.util.Messages;

public class EmailAlreadyInUseException extends ApiException {

	public EmailAlreadyInUseException() {
		super(HttpStatus.CONFLICT, Messages.EMAIL_ALREADY_IN_USE);
	}

}
