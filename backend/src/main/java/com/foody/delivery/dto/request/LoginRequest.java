package com.foody.delivery.dto.request;

import com.foody.delivery.util.Messages;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = Messages.EMAIL_REQUIRED)
		@Email(message = Messages.EMAIL_INVALID)
		String email,

		@NotBlank(message = Messages.PASSWORD_REQUIRED)
		String password) {

	public LoginRequest {
		email = InputNormalizer.email(email);
	}

}
