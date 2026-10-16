package com.foody.delivery.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
		String name,

		@NotBlank(message = "E-mail é obrigatório")
		@Email(message = "E-mail inválido")
		String email,

		@NotBlank(message = "Senha é obrigatória")
		@Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
		String password) {

	public RegisterRequest {
		name = InputNormalizer.trim(name);
		email = InputNormalizer.email(email);
	}

}
