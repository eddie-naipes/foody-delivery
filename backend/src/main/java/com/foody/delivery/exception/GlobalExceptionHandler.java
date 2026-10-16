package com.foody.delivery.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.foody.delivery.dto.response.ApiError;
import com.foody.delivery.util.Messages;

import lombok.extern.slf4j.Slf4j;

/**
 * Converte exceções em respostas no formato padrão {@link ApiError}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		List<ApiError.FieldError> fieldErrors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
			.toList();
		return build(ApiError.of(HttpStatus.BAD_REQUEST, Messages.VALIDATION_FAILED, fieldErrors));
	}

	/** JSON malformado ou valor desconhecido de enum (ex.: status "PRONTO"). */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex) {
		return build(ApiError.of(HttpStatus.BAD_REQUEST, Messages.MALFORMED_REQUEST));
	}

	/** Exceções de negócio: o status vem da própria exceção. */
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException ex) {
		return build(ApiError.of(ex.getStatus(), ex.getMessage()));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
		return build(ApiError.of(HttpStatus.UNAUTHORIZED, Messages.INVALID_CREDENTIALS));
	}

	/** Ex.: GET /api/orders/abc. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return build(ApiError.of(HttpStatus.BAD_REQUEST, Messages.INVALID_PARAMETER.formatted(ex.getName())));
	}

	/**
	 * Último recurso. Erros do próprio Spring MVC (rota inexistente, método não suportado
	 * etc.) mantêm seu status; qualquer outra exceção vira 500 sem expor detalhes internos.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
		if (ex instanceof ErrorResponse frameworkError) {
			HttpStatus status = HttpStatus.valueOf(frameworkError.getStatusCode().value());
			return build(ApiError.of(status, frameworkMessage(status)));
		}
		log.error("Erro inesperado", ex);
		return build(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_ERROR));
	}

	private static String frameworkMessage(HttpStatus status) {
		return switch (status) {
			case NOT_FOUND -> Messages.RESOURCE_NOT_FOUND;
			case METHOD_NOT_ALLOWED -> Messages.METHOD_NOT_ALLOWED;
			default -> Messages.BAD_REQUEST;
		};
	}

	private static ResponseEntity<ApiError> build(ApiError body) {
		return ResponseEntity.status(body.status()).body(body);
	}

}
