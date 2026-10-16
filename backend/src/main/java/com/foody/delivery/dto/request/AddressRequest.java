package com.foody.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
		@NotBlank(message = "Logradouro é obrigatório")
		String street,

		@NotBlank(message = "Número é obrigatório")
		String number,

		String complement,

		@NotBlank(message = "Bairro é obrigatório")
		String neighborhood,

		@NotBlank(message = "Cidade é obrigatória")
		String city,

		@NotBlank(message = "CEP é obrigatório")
		@Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos")
		String zipCode) {

	public AddressRequest {
		street = InputNormalizer.trim(street);
		number = InputNormalizer.trim(number);
		complement = InputNormalizer.optional(complement);
		neighborhood = InputNormalizer.trim(neighborhood);
		city = InputNormalizer.trim(city);
		zipCode = InputNormalizer.zipCode(zipCode);
	}

}
