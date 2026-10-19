package com.foody.delivery.dto.request;

import com.foody.delivery.util.Messages;
import com.foody.delivery.validation.MaxBytes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@Schema(example = "Operador")
		@NotBlank(message = Messages.NAME_REQUIRED)
		@Size(max = 100, message = Messages.NAME_MAX_LENGTH)
		String name,

		@Schema(example = "operador@foody.com")
		@NotBlank(message = Messages.EMAIL_REQUIRED)
		@Email(message = Messages.EMAIL_INVALID)
		String email,

		@Schema(example = "secret123")
		@NotBlank(message = Messages.PASSWORD_REQUIRED)
		@Size(min = 6, message = Messages.PASSWORD_MIN_LENGTH)
		@MaxBytes(value = 72, message = Messages.PASSWORD_TOO_LONG)
		String password) {

	public RegisterRequest {
		name = InputNormalizer.trim(name);
		email = InputNormalizer.email(email);
	}

}
