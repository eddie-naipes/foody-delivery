package com.foody.delivery.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	protected OrderItem() {
		// exigido pelo JPA
	}

	public OrderItem(String name, Integer quantity, BigDecimal unitPrice) {
		this.name = name;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public BigDecimal getSubtotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	void setOrder(Order order) {
		this.order = order;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public Order getOrder() {
		return order;
	}

}
