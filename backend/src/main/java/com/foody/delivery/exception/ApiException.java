package com.foody.delivery.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base das exceções de negócio: cada uma já carrega o status HTTP que representa,
 * então o {@link GlobalExceptionHandler} trata todas com um único método.
 */
@Getter
public abstract class ApiException extends RuntimeException {

	private final HttpStatus status;

	protected ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

}
