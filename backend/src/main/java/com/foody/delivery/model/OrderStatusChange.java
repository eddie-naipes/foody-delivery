package com.foody.delivery.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Uma etapa da linha do tempo do pedido: para qual status ele foi, quem mudou e quando.
 * Imutável e criada só pelo {@link Order}, que é quem conhece as transições válidas.
 */
@Entity
@Table(name = "order_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusChange {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// No SQLite a chave autoincrementada precisa ser INTEGER (alias do rowid), não BIGINT
	@JdbcTypeCode(SqlTypes.INTEGER)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id")
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	/** Nulo apenas em registros reconstruídos de pedidos anteriores ao histórico (migração V3). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by_id")
	private User changedBy;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant changedAt;

	OrderStatusChange(Order order, OrderStatus status, User changedBy) {
		this.order = order;
		this.status = status;
		this.changedBy = changedBy;
	}

}
