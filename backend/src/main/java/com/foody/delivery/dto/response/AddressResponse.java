package com.foody.delivery.dto.response;

import com.foody.delivery.model.Address;

public record AddressResponse(String street, String number, String complement, String neighborhood, String city,
		String zipCode) {

	public static AddressResponse from(Address address) {
		return new AddressResponse(address.getStreet(), address.getNumber(), address.getComplement(),
				address.getNeighborhood(), address.getCity(), address.getZipCode());
	}

}
