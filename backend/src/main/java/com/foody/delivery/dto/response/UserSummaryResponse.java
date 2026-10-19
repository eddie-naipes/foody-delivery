package com.foody.delivery.dto.response;

/** Apenas id e nome de um usuário (quem criou ou alterou um pedido) — nunca o usuário completo. */
public record UserSummaryResponse(Long id, String name) {
}
