package com.foody.delivery.dto.request;

import com.foody.delivery.util.Messages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
		@Schema(example = "Av. Paulista")
		@NotBlank(message = Messages.STREET_REQUIRED)
		String street,

		@Schema(example = "1000")
		@NotBlank(message = Messages.NUMBER_REQUIRED)
		String number,

		@Schema(example = "Apto 12", description = "Opcional")
		String complement,

		@Schema(example = "Bela Vista")
		@NotBlank(message = Messages.NEIGHBORHOOD_REQUIRED)
		String neighborhood,

		@Schema(example = "São Paulo")
		@NotBlank(message = Messages.CITY_REQUIRED)
		String city,

		@Schema(example = "01310-100", description = "8 dígitos, com ou sem hífen")
		@NotBlank(message = Messages.ZIP_CODE_REQUIRED)
		@Pattern(regexp = "\\d{8}", message = Messages.ZIP_CODE_INVALID)
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
