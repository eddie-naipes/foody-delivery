package com.foody.delivery.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.foody.delivery.model.User;
import com.foody.delivery.repository.UserRepository;
import com.foody.delivery.service.JwtService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private UserRepository userRepository;

	private JwtAuthenticationFilter filter;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private MockFilterChain chain;

	@BeforeEach
	void setUp() {
		filter = new JwtAuthenticationFilter(jwtService, userRepository);
		request = new MockHttpServletRequest("GET", "/api/orders");
		response = new MockHttpServletResponse();
		chain = new MockFilterChain();
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void withoutHeaderContinuesUnauthenticated() throws Exception {
		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(jwtService, never()).extractValidSubject(anyString());
	}

	@Test
	void nonBearerHeaderContinuesUnauthenticated() throws Exception {
		request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(jwtService, never()).extractValidSubject(anyString());
	}

	@Test
	void invalidTokenContinuesUnauthenticated() throws Exception {
		request.addHeader("Authorization", "Bearer invalid");
		when(jwtService.extractValidSubject("invalid")).thenReturn(Optional.empty());

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void validTokenOfDeletedUserContinuesUnauthenticated() throws Exception {
		request.addHeader("Authorization", "Bearer valid");
		when(jwtService.extractValidSubject("valid")).thenReturn(Optional.of("gone@foody.com"));
		when(userRepository.findByEmail("gone@foody.com")).thenReturn(Optional.empty());

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void validTokenAuthenticatesUser() throws Exception {
		User user = new User("Maria", "maria@foody.com", "hashed");
		request.addHeader("Authorization", "Bearer valid");
		when(jwtService.extractValidSubject("valid")).thenReturn(Optional.of("maria@foody.com"));
		when(userRepository.findByEmail("maria@foody.com")).thenReturn(Optional.of(user));

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
		assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
	}

	@Test
	void alreadyAuthenticatedIsNotReprocessed() throws Exception {
		SecurityContextHolder.getContext()
			.setAuthentication(UsernamePasswordAuthenticationToken.authenticated("someone", null, java.util.List.of()));
		request.addHeader("Authorization", "Bearer valid");

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
		verify(jwtService, never()).extractValidSubject(anyString());
	}

}
