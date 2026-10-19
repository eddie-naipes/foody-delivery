package com.foody.delivery.dto.request;

import com.foody.delivery.util.Messages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@Schema(example = "operador@foody.com")
		@NotBlank(message = Messages.EMAIL_REQUIRED)
		@Email(message = Messages.EMAIL_INVALID)
		String email,

		@Schema(example = "secret123")
		@NotBlank(message = Messages.PASSWORD_REQUIRED)
		String password) {

	public LoginRequest {
		email = InputNormalizer.email(email);
	}

}
