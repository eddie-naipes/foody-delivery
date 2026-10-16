package com.foody.delivery.dto.response;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * Formato único de erro da API. {@code fieldErrors} só é preenchido em erros de validação.
 */
public record ApiError(int status, String error, String message, List<FieldError> fieldErrors,
		Instant timestamp) {

	public record FieldError(String field, String message) {
	}

	public static ApiError of(HttpStatus status, String message, List<FieldError> fieldErrors) {
		return new ApiError(status.value(), status.getReasonPhrase(), message, fieldErrors, Instant.now());
	}

	public static ApiError of(HttpStatus status, String message) {
		return of(status, message, List.of());
	}

}
