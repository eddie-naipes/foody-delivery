package com.foody.delivery.exception;

public class EmailAlreadyInUseException extends RuntimeException {

	public EmailAlreadyInUseException() {
		super("E-mail já cadastrado");
	}

}
