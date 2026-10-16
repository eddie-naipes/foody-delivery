package com.foody.delivery.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.foody.delivery.dto.response.ApiError;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.util.Messages;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void apiExceptionUsesItsOwnStatusAndMessage() {
		ResponseEntity<ApiError> response = handler
			.handleApiException(new ResourceNotFoundException(Messages.ORDER_NOT_FOUND.formatted(7)));

		assertThat(response.getStatusCode().value()).isEqualTo(404);
		assertThat(response.getBody().error()).isEqualTo("Not Found");
		assertThat(response.getBody().message()).isEqualTo(Messages.ORDER_NOT_FOUND.formatted(7));
	}

	@Test
	void businessExceptionIsLoggedAsWarning(CapturedOutput output) {
		handler.handleApiException(new InvalidStatusTransitionException(OrderStatus.ENTREGUE, OrderStatus.RECEBIDO));

		assertThat(output).contains("WARN").contains("422");
	}

	@Test
	void rejectedLoginIsLoggedAsWarning(CapturedOutput output) {
		handler.handleBadCredentials(new BadCredentialsException("bad"));

		assertThat(output).contains("WARN").contains("Login recusado");
	}

	@Test
	void unexpectedExceptionIsLoggedWithStackTrace(CapturedOutput output) {
		handler.handleUnexpected(new IllegalStateException("falha simulada"));

		assertThat(output).contains("ERROR").contains("falha simulada").contains("at com.foody");
	}

	@Test
	void businessExceptionsCarryTheirHttpStatus() {
		assertThat(new ResourceNotFoundException("x").getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(new EmailAlreadyInUseException().getStatus()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(new InvalidStatusTransitionException(OrderStatus.ENTREGUE, OrderStatus.RECEBIDO).getStatus())
			.isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
	}

	@Test
	void unexpectedExceptionReturns500WithoutDetails() {
		ResponseEntity<ApiError> response = handler
			.handleUnexpected(new IllegalStateException("senha do banco: s3cr3t"));

		assertThat(response.getStatusCode().value()).isEqualTo(500);
		assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
		assertThat(response.getBody().message()).isEqualTo(Messages.INTERNAL_ERROR);
		assertThat(response.getBody().fieldErrors()).isEmpty();
		assertThat(response.getBody().timestamp()).isNotNull();
	}

	@Test
	void methodNotSupportedKeeps405() {
		ResponseEntity<ApiError> response = handler
			.handleUnexpected(new HttpRequestMethodNotSupportedException("DELETE"));

		assertThat(response.getStatusCode().value()).isEqualTo(405);
		assertThat(response.getBody().message()).isEqualTo(Messages.METHOD_NOT_ALLOWED);
	}

	@Test
	void otherFrameworkErrorKeepsItsStatus() {
		ResponseEntity<ApiError> response = handler
			.handleUnexpected(new HttpMediaTypeNotSupportedException("text/plain"));

		assertThat(response.getStatusCode().value()).isEqualTo(415);
		assertThat(response.getBody().message()).isEqualTo(Messages.BAD_REQUEST);
	}

}
