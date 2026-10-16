package com.foody.delivery.dto.request;

import java.util.Locale;

/**
 * Normaliza a entrada antes da validação, para que " Maria@X.com " e "maria@x.com"
 * sejam tratados como o mesmo e-mail. Valores nulos são preservados para que o Bean
 * Validation reporte o campo obrigatório.
 */
final class InputNormalizer {

	private InputNormalizer() {
	}

	static String trim(String value) {
		return value == null ? null : value.trim();
	}

	static String email(String value) {
		return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
	}

}
