package com.foody.delivery.dto.request;

import com.foody.delivery.util.Messages;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = Messages.NAME_REQUIRED)
		@Size(max = 100, message = Messages.NAME_MAX_LENGTH)
		String name,

		@NotBlank(message = Messages.EMAIL_REQUIRED)
		@Email(message = Messages.EMAIL_INVALID)
		String email,

		@NotBlank(message = Messages.PASSWORD_REQUIRED)
		@Size(min = 6, message = Messages.PASSWORD_MIN_LENGTH)
		String password) {

	public RegisterRequest {
		name = InputNormalizer.trim(name);
		email = InputNormalizer.email(email);
	}

}
