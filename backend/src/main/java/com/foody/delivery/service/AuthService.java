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
import com.foody.delivery.exception.EmailAlreadyInUseException;
import com.foody.delivery.mapper.UserMapper;
import com.foody.delivery.model.User;
import com.foody.delivery.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	private final UserMapper userMapper;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyInUseException();
		}
		User user = userRepository
			.save(new User(request.name(), request.email(), passwordEncoder.encode(request.password())));
		log.info("Usuário cadastrado: id={}", user.getId());
		return toResponse(user);
	}

	/**
	 * Delega a verificação de credenciais ao Spring Security. Credenciais inválidas
	 * resultam em {@link org.springframework.security.authentication.BadCredentialsException}.
	 */
	public AuthResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
		User user = (User) authentication.getPrincipal();
		log.info("Login realizado: usuário id={}", user.getId());
		return toResponse(user);
	}

	private AuthResponse toResponse(User user) {
		return new AuthResponse(jwtService.generateToken(user), userMapper.toResponse(user));
	}

}
