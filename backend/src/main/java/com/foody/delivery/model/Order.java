package com.foody.delivery.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.foody.delivery.exception.InvalidStatusTransitionException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pedido de delivery. Sem setters: o status só muda por {@link #changeStatus}, que
 * aplica as regras de transição, e itens só entram por {@link #addItem}.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String customerName;

	@Column(nullable = false)
	private String customerPhone;

	@Embedded
	private Address deliveryAddress;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status = OrderStatus.RECEBIDO;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_id")
	private User createdBy;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	public Order(String customerName, String customerPhone, Address deliveryAddress, User createdBy) {
		this.customerName = customerName;
		this.customerPhone = customerPhone;
		this.deliveryAddress = deliveryAddress;
		this.createdBy = createdBy;
	}

	public void addItem(OrderItem item) {
		item.setOrder(this);
		items.add(item);
	}

	/** Soma dos subtotais. Calculado, não persistido, para nunca ficar inconsistente com os itens. */
	public BigDecimal getTotalAmount() {
		return items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void changeStatus(OrderStatus next) {
		if (!status.canTransitionTo(next)) {
			throw new InvalidStatusTransitionException(status, next);
		}
		this.status = next;
	}

	public List<OrderItem> getItems() {
		return Collections.unmodifiableList(items);
	}

}
