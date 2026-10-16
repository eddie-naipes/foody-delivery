package com.foody.delivery.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.foody.delivery.dto.response.ErrorResponse;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void unexpectedExceptionReturns500WithoutDetails() {
		ResponseEntity<ErrorResponse> response = handler
			.handleUnexpected(new IllegalStateException("senha do banco: s3cr3t"));

		assertThat(response.getStatusCode().value()).isEqualTo(500);
		assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
		assertThat(response.getBody().message()).isEqualTo("Erro interno do servidor");
		assertThat(response.getBody().fieldErrors()).isEmpty();
		assertThat(response.getBody().timestamp()).isNotNull();
	}

	@Test
	void methodNotSupportedKeeps405() {
		ResponseEntity<ErrorResponse> response = handler
			.handleUnexpected(new HttpRequestMethodNotSupportedException("DELETE"));

		assertThat(response.getStatusCode().value()).isEqualTo(405);
		assertThat(response.getBody().message()).isEqualTo("Método não permitido");
	}

	@Test
	void otherFrameworkErrorKeepsItsStatus() {
		ResponseEntity<ErrorResponse> response = handler
			.handleUnexpected(new HttpMediaTypeNotSupportedException("text/plain"));

		assertThat(response.getStatusCode().value()).isEqualTo(415);
		assertThat(response.getBody().message()).isEqualTo("Requisição inválida");
	}

}
