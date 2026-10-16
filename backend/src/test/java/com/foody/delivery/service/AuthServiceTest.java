package com.foody.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.foody.delivery.dto.request.LoginRequest;
import com.foody.delivery.dto.request.RegisterRequest;
import com.foody.delivery.dto.response.AuthResponse;
import com.foody.delivery.exception.EmailAlreadyInUseException;
import com.foody.delivery.mapper.UserMapper;
import com.foody.delivery.model.User;
import com.foody.delivery.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Spy
	private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

	@InjectMocks
	private AuthService authService;

	@Test
	void registerHashesPasswordAndReturnsToken() {
		when(passwordEncoder.encode("secret123")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
		when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

		AuthResponse response = authService.register(new RegisterRequest("Maria", "maria@foody.com", "secret123"));

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(saved.capture());
		assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.user().name()).isEqualTo("Maria");
		assertThat(response.user().email()).isEqualTo("maria@foody.com");
	}


	@Test
	void registerRejectsDuplicateEmail() {
		when(userRepository.existsByEmail("maria@foody.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(new RegisterRequest("Maria", "maria@foody.com", "secret123")))
			.isInstanceOf(EmailAlreadyInUseException.class)
			.hasMessage("E-mail já cadastrado");
		verify(userRepository, never()).save(any());
	}

	@Test
	void loginAuthenticatesAndReturnsToken() {
		User user = new User("Maria", "maria@foody.com", "hashed");
		Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
		when(authenticationManager.authenticate(any())).thenReturn(authenticated);
		when(jwtService.generateToken(user)).thenReturn("jwt-token");

		AuthResponse response = authService.login(new LoginRequest("maria@foody.com", "secret123"));

		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.user().email()).isEqualTo("maria@foody.com");
	}

	@Test
	void loginDelegatesCredentialsToAuthenticationManager() {
		User user = new User("Maria", "maria@foody.com", "hashed");
		when(authenticationManager.authenticate(any()))
			.thenReturn(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));

		authService.login(new LoginRequest("maria@foody.com", "secret123"));

		ArgumentCaptor<Authentication> attempt = ArgumentCaptor.forClass(Authentication.class);
		verify(authenticationManager).authenticate(attempt.capture());
		assertThat(attempt.getValue().getPrincipal()).isEqualTo("maria@foody.com");
		assertThat(attempt.getValue().getCredentials()).isEqualTo("secret123");
	}

	@Test
	void loginPropagatesBadCredentials() {
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("maria@foody.com", "wrong")))
			.isInstanceOf(BadCredentialsException.class);
	}

}
