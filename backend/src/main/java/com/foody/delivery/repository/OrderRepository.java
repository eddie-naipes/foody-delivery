package com.foody.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.foody.delivery.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	/** Lista todos os pedidos, mais recentes primeiro. */
	default List<Order> findAllNewestFirst() {
		return findAllByOrderByCreatedAtDescIdDesc();
	}

	/**
	 * Consulta derivada pelo nome; o id desempata pedidos criados no mesmo instante.
	 * Itens e criador vêm na mesma consulta, evitando N+1 ao montar a lista.
	 */
	@EntityGraph(attributePaths = { "items", "createdBy" })
	List<Order> findAllByOrderByCreatedAtDescIdDesc();

}
