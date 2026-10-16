package com.foody.delivery.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.foody.delivery.dto.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Converte exceções em respostas no formato padrão {@link ErrorResponse}.
 */
@Slf4j
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

	/** Ex.: GET /api/orders/abc. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return build(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName()));
	}

	/**
	 * Último recurso. Erros do próprio Spring MVC (rota inexistente, método não suportado
	 * etc.) mantêm seu status; qualquer outra exceção vira 500 sem expor detalhes internos.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		if (ex instanceof org.springframework.web.ErrorResponse frameworkError) {
			HttpStatus status = HttpStatus.valueOf(frameworkError.getStatusCode().value());
			return build(ErrorResponse.of(status, frameworkMessage(status)));
		}
		log.error("Erro inesperado", ex);
		return build(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor"));
	}

	private static String frameworkMessage(HttpStatus status) {
		return switch (status) {
			case NOT_FOUND -> "Recurso não encontrado";
			case METHOD_NOT_ALLOWED -> "Método não permitido";
			default -> "Requisição inválida";
		};
	}

	private static ResponseEntity<ErrorResponse> build(ErrorResponse body) {
		return ResponseEntity.status(body.status()).body(body);
	}

}
