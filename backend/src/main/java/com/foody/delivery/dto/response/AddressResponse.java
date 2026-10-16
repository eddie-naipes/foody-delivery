package com.foody.delivery.dto.response;

public record AddressResponse(String street, String number, String complement, String neighborhood, String city,
		String zipCode) {
}
