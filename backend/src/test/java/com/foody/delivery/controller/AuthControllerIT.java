package com.foody.delivery.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.foody.delivery.repository.OrderRepository;
import com.foody.delivery.repository.UserRepository;
import com.foody.delivery.service.JwtService;
import com.foody.delivery.util.Messages;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@BeforeEach
	void cleanDatabase() {
		orderRepository.deleteAll();
		userRepository.deleteAll();
	}

	private ResultActions register(String name, String email, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"name": "%s", "email": "%s", "password": "%s"}
					""".formatted(name, email, password)));
	}

	private ResultActions login(String email, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"email": "%s", "password": "%s"}
					""".formatted(email, password)));
	}

	@Test
	void registerReturns201WithToken() throws Exception {
		register("Maria", "maria@foody.com", "secret123")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").value(not(emptyString())))
			.andExpect(jsonPath("$.user.id").isNumber())
			.andExpect(jsonPath("$.user.name").value("Maria"))
			.andExpect(jsonPath("$.user.email").value("maria@foody.com"))
			.andExpect(jsonPath("$.user.password").doesNotExist());
	}

	@Test
	void registerDuplicateEmailReturns409() throws Exception {
		register("Maria", "maria@foody.com", "secret123");

		register("Outra", "MARIA@foody.com", "secret456")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(Messages.EMAIL_ALREADY_IN_USE));
	}

	@Test
	void registerInvalidBodyReturns400() throws Exception {
		register("", "not-an-email", "123")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")))
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("email")))
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("password")));
	}

	@Test
	void registerWithPasswordOver72BytesReturns400() throws Exception {
		register("Maria", "maria@foody.com", "á".repeat(40))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')].message")
				.value(hasItem(Messages.PASSWORD_TOO_LONG)));
	}

	@Test
	void protectedRouteWithExpiredTokenReturns401() throws Exception {
		register("Maria", "maria@foody.com", "secret123");
		Clock nineHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(9)), ZoneOffset.UTC);
		String expired = new JwtService(jwtSecret, Duration.ofHours(8), nineHoursAgo)
			.generateToken(userRepository.findByEmail("maria@foody.com").orElseThrow());

		mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + expired))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value(Messages.AUTHENTICATION_REQUIRED));
	}

	@Test
	void loginReturns200WithToken() throws Exception {
		register("Maria", "maria@foody.com", "secret123");

		login(" Maria@Foody.com ", "secret123")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").value(not(emptyString())))
			.andExpect(jsonPath("$.user.email").value("maria@foody.com"));
	}

	@Test
	void loginWrongPasswordReturns401() throws Exception {
		register("Maria", "maria@foody.com", "secret123");

		login("maria@foody.com", "wrong-pass")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value(Messages.INVALID_CREDENTIALS));
	}

	@Test
	void loginUnknownEmailReturns401() throws Exception {
		login("nobody@foody.com", "secret123")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value(Messages.INVALID_CREDENTIALS));
	}

	@Test
	void protectedRouteWithoutTokenReturns401() throws Exception {
		mockMvc.perform(get("/api/orders"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value(Messages.AUTHENTICATION_REQUIRED));
	}

	@Test
	void protectedRouteWithTamperedTokenReturns401() throws Exception {
		String body = register("Maria", "maria@foody.com", "secret123").andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(body, "$.token");
		String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

		mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + tampered))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void corsPreflightFromFrontendIsAllowed() throws Exception {
		mockMvc.perform(options("/api/orders")
			.header("Origin", "http://localhost:5173")
			.header("Access-Control-Request-Method", "POST")
			.header("Access-Control-Request-Headers", "Authorization,Content-Type"))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
	}

}
