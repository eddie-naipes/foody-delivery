package com.foody.delivery.dto.request;

import com.foody.delivery.util.Messages;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(
		@NotBlank(message = Messages.STREET_REQUIRED)
		String street,

		@NotBlank(message = Messages.NUMBER_REQUIRED)
		String number,

		String complement,

		@NotBlank(message = Messages.NEIGHBORHOOD_REQUIRED)
		String neighborhood,

		@NotBlank(message = Messages.CITY_REQUIRED)
		String city,

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
