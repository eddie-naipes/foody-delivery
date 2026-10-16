package com.foody.delivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Endereço de entrega. Embutido na tabela de pedidos (não tem tabela própria).
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

	@Column(nullable = false)
	private String street;

	@Column(nullable = false)
	private String number;

	private String complement;

	@Column(nullable = false)
	private String neighborhood;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false, length = 8)
	private String zipCode;

	public Address(String street, String number, String complement, String neighborhood, String city,
			String zipCode) {
		this.street = street;
		this.number = number;
		this.complement = complement;
		this.neighborhood = neighborhood;
		this.city = city;
		this.zipCode = zipCode;
	}

}
