package com.foody.delivery.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Página de resultados com um contrato próprio e estável, em vez de serializar o
 * {@link Page} do Spring Data, cujo formato JSON não é garantido entre versões.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages());
	}

}
