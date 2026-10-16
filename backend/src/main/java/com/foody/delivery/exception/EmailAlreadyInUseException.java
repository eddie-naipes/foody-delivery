package com.foody.delivery.exception;

import com.foody.delivery.util.Messages;

public class EmailAlreadyInUseException extends RuntimeException {

	public EmailAlreadyInUseException() {
		super(Messages.EMAIL_ALREADY_IN_USE);
	}

}
