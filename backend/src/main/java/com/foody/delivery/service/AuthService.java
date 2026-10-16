package com.foody.delivery.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foody.delivery.dto.request.LoginRequest;
import com.foody.delivery.dto.request.RegisterRequest;
import com.foody.delivery.dto.response.AuthResponse;
import com.foody.delivery.dto.response.UserResponse;
import com.foody.delivery.exception.EmailAlreadyInUseException;
import com.foody.delivery.model.User;
import com.foody.delivery.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyInUseException();
		}
		User user = userRepository
			.save(new User(request.name(), request.email(), passwordEncoder.encode(request.password())));
		return toResponse(user);
	}

	/**
	 * Delega a verificação de credenciais ao Spring Security. Credenciais inválidas
	 * resultam em {@link org.springframework.security.authentication.BadCredentialsException}.
	 */
	public AuthResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
		return toResponse((User) authentication.getPrincipal());
	}

	private AuthResponse toResponse(User user) {
		return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
	}

}
