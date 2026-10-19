package com.foody.delivery.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderStatus;

/**
 * Na listagem paginada só o criador vem por join: buscar coleções (itens, histórico) com
 * join faria o Hibernate paginar em memória. As coleções chegam em lote
 * ({@code default_batch_fetch_size}), com uma consulta por coleção para a página toda.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

	/** Mais recentes primeiro; o id desempata pedidos criados no mesmo instante. */
	Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	@Override
	@EntityGraph(attributePaths = "createdBy")
	Page<Order> findAll(Pageable pageable);

	@EntityGraph(attributePaths = "createdBy")
	Page<Order> findByStatus(OrderStatus status, Pageable pageable);

	@Query("select new com.foody.delivery.repository.StatusCount(o.status, count(o)) from Order o group by o.status")
	List<StatusCount> countByStatus();

}
