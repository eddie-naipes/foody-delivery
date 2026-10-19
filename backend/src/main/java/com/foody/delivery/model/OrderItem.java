package com.foody.delivery.model;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// No SQLite a chave autoincrementada precisa ser INTEGER (alias do rowid), não BIGINT
	@JdbcTypeCode(SqlTypes.INTEGER)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer quantity;

	/** Preço no momento do pedido (snapshot). */
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id")
	private Order order;

	public OrderItem(String name, Integer quantity, BigDecimal unitPrice) {
		this.name = name;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public BigDecimal getSubtotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	/** Só o {@link Order#addItem} liga um item ao pedido, mantendo os dois lados consistentes. */
	void setOrder(Order order) {
		this.order = order;
	}

}
