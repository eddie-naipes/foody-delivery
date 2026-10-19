package com.foody.delivery.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Documentação interativa (Swagger UI em {@code /swagger-ui.html}). O botão "Authorize"
 * recebe o token devolvido pelo cadastro/login e o envia como {@code Bearer} nas rotas de pedidos.
 */
@Configuration
@OpenAPIDefinition(
		info = @Info(title = "Foody Delivery API", version = "1.0",
				description = "API REST de rastreamento de pedidos de delivery. Cadastre-se ou entre em "
						+ "**Autenticação**, copie o `token` da resposta e clique em **Authorize**."),
		// Relativo: funciona igual em localhost, no Docker (atrás do Nginx) e em produção
		servers = @Server(url = "/", description = "Este servidor"))
@SecurityScheme(name = OpenApiConfig.BEARER_SCHEME, type = SecuritySchemeType.HTTP, scheme = "bearer",
		bearerFormat = "JWT")
public class OpenApiConfig {

	public static final String BEARER_SCHEME = "bearerAuth";

}
