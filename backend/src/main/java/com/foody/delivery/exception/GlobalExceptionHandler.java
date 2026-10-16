package com.foody.delivery.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.foody.delivery.dto.response.ErrorResponse;

/**
 * Converte exceções em respostas no formato padrão {@link ErrorResponse}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
			.toList();
		return build(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Dados inválidos", fieldErrors));
	}

	/** JSON malformado ou valor desconhecido de enum (ex.: status "PRONTO"). */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
		return build(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido"));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
		return build(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage()));
	}

	@ExceptionHandler(InvalidStatusTransitionException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
		return build(ErrorResponse.of(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage()));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
		return build(ErrorResponse.of(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));
	}

	@ExceptionHandler(EmailAlreadyInUseException.class)
	public ResponseEntity<ErrorResponse> handleEmailAlreadyInUse(EmailAlreadyInUseException ex) {
		return build(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
	}

	private static ResponseEntity<ErrorResponse> build(ErrorResponse body) {
		return ResponseEntity.status(body.status()).body(body);
	}

}
