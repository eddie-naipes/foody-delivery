package com.foody.delivery.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.foody.delivery.repository.UserRepository;
import com.foody.delivery.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Lê o header {@code Authorization: Bearer <token>} e, se o token for válido,
 * autentica o usuário no {@code SecurityContext}. Nunca bloqueia a requisição:
 * quem decide se a rota exige autenticação é o {@code SecurityConfig}.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			jwtService.extractValidSubject(header.substring(BEARER_PREFIX.length()))
				.flatMap(userRepository::findByEmail)
				.ifPresent(user -> {
					var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null,
							user.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				});
		}
		chain.doFilter(request, response);
	}

}
