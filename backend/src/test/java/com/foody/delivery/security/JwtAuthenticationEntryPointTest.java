package com.foody.delivery.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import tools.jackson.databind.json.JsonMapper;

class JwtAuthenticationEntryPointTest {

	@Test
	void writesStandard401Json() throws Exception {
		JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(JsonMapper.builder().build());
		MockHttpServletResponse response = new MockHttpServletResponse();

		entryPoint.commence(new MockHttpServletRequest(), response, new InsufficientAuthenticationException("x"));

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).startsWith("application/json");
		assertThat(response.getContentAsString())
			.contains("\"status\":401")
			.contains("\"error\":\"Unauthorized\"")
			.contains("\"message\":\"Autenticação necessária\"");
	}

}
