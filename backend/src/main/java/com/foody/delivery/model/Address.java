package com.foody.delivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereço de entrega. Embutido na tabela de pedidos (não tem tabela própria).
 */
@Embeddable
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

	protected Address() {
		// exigido pelo JPA
	}

	public Address(String street, String number, String complement, String neighborhood, String city,
			String zipCode) {
		this.street = street;
		this.number = number;
		this.complement = complement;
		this.neighborhood = neighborhood;
		this.city = city;
		this.zipCode = zipCode;
	}

	public String getStreet() {
		return street;
	}

	public String getNumber() {
		return number;
	}

	public String getComplement() {
		return complement;
	}

	public String getNeighborhood() {
		return neighborhood;
	}

	public String getCity() {
		return city;
	}

	public String getZipCode() {
		return zipCode;
	}

}
