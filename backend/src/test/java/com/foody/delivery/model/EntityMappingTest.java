package com.foody.delivery.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Garante que o mapeamento JPA das entidades funciona no SQLite: grava, limpa o
 * contexto de persistência e recarrega do banco.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EntityMappingTest {

	@Autowired
	private TestEntityManager em;

	@Test
	void persistsAndReloadsUser() {
		User saved = em.persistFlushFind(new User("Maria", "maria@foody.com", "hash"));
		em.clear();

		User reloaded = em.find(User.class, saved.getId());

		assertThat(reloaded.getId()).isNotNull();
		assertThat(reloaded.getEmail()).isEqualTo("maria@foody.com");
		assertThat(reloaded.getCreatedAt()).isNotNull();
	}

	@Test
	void persistsAndReloadsOrderWithItemsAndAddress() {
		User user = em.persist(new User("Operador", "op@foody.com", "hash"));
		Order order = new Order("Maria Silva", "11999998888",
				new Address("Av. Paulista", "1000", "Apto 12", "Bela Vista", "São Paulo", "01310100"), user);
		order.addItem(new OrderItem("Pizza", 2, new BigDecimal("45.00")));
		order.addItem(new OrderItem("Refrigerante", 1, new BigDecimal("9.90")));
		Long id = em.persistAndGetId(order, Long.class);
		em.flush();
		em.clear();

		Order reloaded = em.find(Order.class, id);

		assertThat(reloaded.getId()).isEqualTo(id);
		assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.RECEBIDO);
		assertThat(reloaded.getCreatedAt()).isNotNull();
		assertThat(reloaded.getUpdatedAt()).isNotNull();
		assertThat(reloaded.getCreatedBy().getEmail()).isEqualTo("op@foody.com");
		assertThat(reloaded.getDeliveryAddress().getCity()).isEqualTo("São Paulo");
		assertThat(reloaded.getDeliveryAddress().getComplement()).isEqualTo("Apto 12");
		assertThat(reloaded.getItems()).hasSize(2);
		assertThat(reloaded.getItems().get(0).getId()).isNotNull();
		assertThat(reloaded.getTotalAmount()).isEqualByComparingTo("99.90");
	}

	@Test
	void persistsAndReloadsStatusHistoryInOrder() {
		User creator = em.persist(new User("Operador", "op@foody.com", "hash"));
		User courier = em.persist(new User("Expedição", "expedicao@foody.com", "hash"));
		Order order = new Order("Maria Silva", "11999998888",
				new Address("Rua A", "1", null, "Centro", "São Paulo", "01001000"), creator);
		order.addItem(new OrderItem("Pizza", 1, new BigDecimal("45.00")));
		Long id = em.persistAndGetId(order, Long.class);
		em.flush();
		order.changeStatus(OrderStatus.EM_PREPARO, courier);
		em.flush();
		em.clear();

		Order reloaded = em.find(Order.class, id);

		assertThat(reloaded.getStatusHistory()).extracting(OrderStatusChange::getStatus)
			.containsExactly(OrderStatus.RECEBIDO, OrderStatus.EM_PREPARO);
		assertThat(reloaded.getStatusHistory()).extracting(change -> change.getChangedBy().getName())
			.containsExactly("Operador", "Expedição");
		assertThat(reloaded.getStatusHistory()).allSatisfy(change -> {
			assertThat(change.getId()).isNotNull();
			assertThat(change.getChangedAt()).isNotNull();
		});
	}

}
