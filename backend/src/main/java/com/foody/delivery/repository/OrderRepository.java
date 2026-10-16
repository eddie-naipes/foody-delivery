package com.foody.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foody.delivery.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	/** Mais recentes primeiro; o id desempata pedidos criados no mesmo instante. */
	List<Order> findAllByOrderByCreatedAtDescIdDesc();

}
