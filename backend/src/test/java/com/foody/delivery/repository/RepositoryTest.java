package com.foody.delivery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
import com.foody.delivery.model.OrderStatus;
import com.foody.delivery.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	private User user;

	@BeforeEach
	void setUp() {
		user = userRepository.save(new User("Operador", "op@foody.com", "hash"));
	}

	@Test
	void findByEmailReturnsUser() {
		assertThat(userRepository.findByEmail("op@foody.com")).contains(user);
		assertThat(userRepository.findByEmail("nobody@foody.com")).isEmpty();
	}

	@Test
	void existsByEmail() {
		assertThat(userRepository.existsByEmail("op@foody.com")).isTrue();
		assertThat(userRepository.existsByEmail("nobody@foody.com")).isFalse();
	}

	@Test
	void findAllPagesMostRecentFirst() {
		Order first = orderRepository.save(newOrder("Primeiro"));
		Order second = orderRepository.save(newOrder("Segundo"));
		Order third = orderRepository.save(newOrder("Terceiro"));

		Page<Order> firstPage = orderRepository.findAll(PageRequest.of(0, 2, OrderRepository.NEWEST_FIRST));
		Page<Order> secondPage = orderRepository.findAll(PageRequest.of(1, 2, OrderRepository.NEWEST_FIRST));

		assertThat(firstPage.getContent()).extracting(Order::getId).containsExactly(third.getId(), second.getId());
		assertThat(secondPage.getContent()).extracting(Order::getId).containsExactly(first.getId());
		assertThat(firstPage.getTotalElements()).isEqualTo(3);
	}

	@Test
	void findByStatusReturnsOnlyThatStatus() {
		orderRepository.save(newOrder("Recebido"));
		Order preparing = newOrder("Em preparo");
		preparing.changeStatus(OrderStatus.EM_PREPARO, user);
		orderRepository.save(preparing);

		Page<Order> page = orderRepository.findByStatus(OrderStatus.EM_PREPARO,
				PageRequest.of(0, 20, OrderRepository.NEWEST_FIRST));

		assertThat(page.getContent()).extracting(Order::getCustomerName).containsExactly("Em preparo");
	}

	@Test
	void countByStatusGroupsOnlyExistingStatuses() {
		orderRepository.save(newOrder("A"));
		orderRepository.save(newOrder("B"));
		Order canceled = newOrder("C");
		canceled.changeStatus(OrderStatus.CANCELADO, user);
		orderRepository.save(canceled);

		assertThat(orderRepository.countByStatus()).containsExactlyInAnyOrder(
				new StatusCount(OrderStatus.RECEBIDO, 2L), new StatusCount(OrderStatus.CANCELADO, 1L));
	}

	private Order newOrder(String customer) {
		return new Order(customer, "11999998888",
				new Address("Rua A", "1", null, "Centro", "São Paulo", "01001000"), user);
	}

}
