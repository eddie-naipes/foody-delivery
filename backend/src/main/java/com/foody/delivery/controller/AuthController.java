package com.foody.delivery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.foody.delivery.dto.request.LoginRequest;
import com.foody.delivery.dto.request.RegisterRequest;
import com.foody.delivery.dto.response.ApiError;
import com.foody.delivery.dto.response.AuthResponse;
import com.foody.delivery.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro e login. Devolvem o token JWT, válido por 8 horas.")
@ApiResponse(responseCode = "400", description = "Dados inválidos, com o erro de cada campo",
		content = @Content(schema = @Schema(implementation = ApiError.class)))
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um operador", description = "Já devolve o token: não é preciso fazer login depois.")
	@ApiResponse(responseCode = "201", description = "Cadastrado e autenticado")
	@ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	@Operation(summary = "Entra com e-mail e senha")
	@ApiResponse(responseCode = "200", description = "Autenticado")
	@ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
			content = @Content(schema = @Schema(implementation = ApiError.class)))
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

}
