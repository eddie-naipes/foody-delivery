package com.foody.delivery.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

	private final User user = new User("Maria", "maria@foody.com", "hash");

	@Test
	void usernameIsEmail() {
		assertThat(user.getUsername()).isEqualTo("maria@foody.com");
	}

	@Test
	void authoritiesAreEmpty() {
		assertThat(user.getAuthorities()).isEmpty();
	}

	@Test
	void exposesConstructorValues() {
		assertThat(user.getName()).isEqualTo("Maria");
		assertThat(user.getEmail()).isEqualTo("maria@foody.com");
		assertThat(user.getPassword()).isEqualTo("hash");
	}

}
