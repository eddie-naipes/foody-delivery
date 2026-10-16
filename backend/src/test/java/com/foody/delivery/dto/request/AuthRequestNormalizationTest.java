package com.foody.delivery.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthRequestNormalizationTest {

	@Test
	void registerRequestNormalizesEmailAndTrimsName() {
		RegisterRequest request = new RegisterRequest("  Maria  ", " Maria@Foody.COM ", "secret123");

		assertThat(request.name()).isEqualTo("Maria");
		assertThat(request.email()).isEqualTo("maria@foody.com");
		assertThat(request.password()).isEqualTo("secret123");
	}

	@Test
	void registerRequestKeepsNullsForValidation() {
		RegisterRequest request = new RegisterRequest(null, null, null);

		assertThat(request.name()).isNull();
		assertThat(request.email()).isNull();
	}

	@Test
	void loginRequestNormalizesEmail() {
		LoginRequest request = new LoginRequest(" Maria@Foody.COM ", "secret123");

		assertThat(request.email()).isEqualTo("maria@foody.com");
		assertThat(request.password()).isEqualTo("secret123");
	}

	@Test
	void loginRequestKeepsNullEmailForValidation() {
		assertThat(new LoginRequest(null, "x").email()).isNull();
	}

}
