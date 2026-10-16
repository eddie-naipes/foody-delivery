package com.foody.delivery.model;

/**
 * Status do pedido e suas regras de transição.
 *
 * <pre>
 * RECEBIDO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE
 *     ↓           ↓               ↓
 *  CANCELADO   CANCELADO      CANCELADO
 * </pre>
 *
 * Um pedido só avança para o próximo passo ou é cancelado. ENTREGUE e CANCELADO são finais.
 */
public enum OrderStatus {

	RECEBIDO,
	EM_PREPARO,
	SAIU_PARA_ENTREGA,
	ENTREGUE,
	CANCELADO;

	public boolean canTransitionTo(OrderStatus next) {
		if (next == null || isFinal()) {
			return false;
		}
		return next == CANCELADO || next == nextStep();
	}

	public boolean isFinal() {
		return nextStep() == null;
	}

	private OrderStatus nextStep() {
		return switch (this) {
			case RECEBIDO -> EM_PREPARO;
			case EM_PREPARO -> SAIU_PARA_ENTREGA;
			case SAIU_PARA_ENTREGA -> ENTREGUE;
			case ENTREGUE, CANCELADO -> null;
		};
	}

}
