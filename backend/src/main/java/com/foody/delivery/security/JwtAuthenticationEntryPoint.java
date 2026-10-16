package com.foody.delivery.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.foody.delivery.dto.response.ApiError;
import com.foody.delivery.util.Messages;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * Responde 401 no formato padrão de erro quando uma rota protegida é acessada sem
 * token válido.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final JsonMapper jsonMapper;

	public JwtAuthenticationEntryPoint(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		jsonMapper.writeValue(response.getOutputStream(),
				ApiError.of(HttpStatus.UNAUTHORIZED, Messages.AUTHENTICATION_REQUIRED));
	}

}
