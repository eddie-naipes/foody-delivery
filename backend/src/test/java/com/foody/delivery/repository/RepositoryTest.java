package com.foody.delivery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.foody.delivery.model.Address;
import com.foody.delivery.model.Order;
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
	void findAllReturnsMostRecentFirst() {
		Order first = orderRepository.save(newOrder("Primeiro"));
		Order second = orderRepository.save(newOrder("Segundo"));

		assertThat(orderRepository.findAllByOrderByCreatedAtDescIdDesc())
			.extracting(Order::getId)
			.containsExactly(second.getId(), first.getId());
	}

	private Order newOrder(String customer) {
		return new Order(customer, "11999998888",
				new Address("Rua A", "1", null, "Centro", "São Paulo", "01001000"), user);
	}

}
