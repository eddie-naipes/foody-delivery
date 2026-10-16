package com.foody.delivery.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AddressTest {

	@Test
	void exposesConstructorValues() {
		Address address = new Address("Av. Paulista", "1000", "Apto 12", "Bela Vista", "São Paulo", "01310100");

		assertThat(address.getStreet()).isEqualTo("Av. Paulista");
		assertThat(address.getNumber()).isEqualTo("1000");
		assertThat(address.getComplement()).isEqualTo("Apto 12");
		assertThat(address.getNeighborhood()).isEqualTo("Bela Vista");
		assertThat(address.getCity()).isEqualTo("São Paulo");
		assertThat(address.getZipCode()).isEqualTo("01310100");
	}

}
