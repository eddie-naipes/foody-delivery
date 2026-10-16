package com.foody.delivery.dto.request;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normaliza a entrada antes da validação, para que " Maria@X.com " e "maria@x.com"
 * sejam tratados como o mesmo e-mail. Valores nulos são preservados para que o Bean
 * Validation reporte o campo obrigatório.
 */
final class InputNormalizer {

	private static final Pattern HYPHENATED_ZIP_CODE = Pattern.compile("\\d{5}-\\d{3}");

	private InputNormalizer() {
	}

	static String trim(String value) {
		return value == null ? null : value.trim();
	}

	static String email(String value) {
		return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
	}

	/** Campo opcional: texto em branco vira nulo. */
	static String optional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	/**
	 * Aceita "01310-100" ou "01310100" e guarda só os dígitos. Qualquer outro formato
	 * é mantido como veio, para o {@code @Pattern} rejeitá-lo.
	 */
	static String zipCode(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return HYPHENATED_ZIP_CODE.matcher(trimmed).matches() ? trimmed.replace("-", "") : trimmed;
	}

}
