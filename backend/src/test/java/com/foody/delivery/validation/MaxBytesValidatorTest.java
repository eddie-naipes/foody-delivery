package com.foody.delivery.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MaxBytesValidatorTest {

	private final MaxBytesValidator validator = new MaxBytesValidator();

	@BeforeEach
	void setUp() {
		MaxBytes annotation = Mockito.mock(MaxBytes.class);
		Mockito.when(annotation.value()).thenReturn(72);
		validator.initialize(annotation);
	}

	@Test
	void nullIsLeftForNotBlank() {
		assertThat(validator.isValid(null, null)).isTrue();
	}

	@Test
	void acceptsUpToLimitInUtf8Bytes() {
		assertThat(validator.isValid("a".repeat(72), null)).isTrue();
		assertThat(validator.isValid("á".repeat(36), null)).isTrue();
	}

	@Test
	void rejectsOverLimitEvenWithFewCharacters() {
		assertThat(validator.isValid("a".repeat(73), null)).isFalse();
		assertThat(validator.isValid("á".repeat(37), null)).isFalse();
	}

}
