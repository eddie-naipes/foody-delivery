package com.foody.delivery.observability;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Dá a cada requisição um identificador que aparece em todas as linhas de log dela e
 * volta no header {@code X-Request-Id}. Se o proxy (Nginx) já mandou um, ele é
 * reaproveitado, ligando o log de acesso do Nginx ao da API. Ao final, registra
 * método, rota, status e duração.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Request-Id";

	public static final String MDC_KEY = "requestId";

	/** Só aceita ids "bem-comportados" vindos de fora, para não injetar lixo nos logs. */
	private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9-]{8,64}");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String requestId = resolveRequestId(request.getHeader(HEADER));
		long start = System.nanoTime();
		MDC.put(MDC_KEY, requestId);
		response.setHeader(HEADER, requestId);
		try {
			chain.doFilter(request, response);
		}
		finally {
			long millis = (System.nanoTime() - start) / 1_000_000;
			log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), millis);
			MDC.remove(MDC_KEY);
		}
	}

	/** O health check do Docker roda a cada poucos segundos: fica fora do log de acesso. */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getRequestURI().startsWith("/actuator/");
	}

	static String resolveRequestId(String incoming) {
		return incoming != null && SAFE_ID.matcher(incoming).matches() ? incoming : UUID.randomUUID().toString();
	}

}
