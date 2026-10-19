package com.foody.delivery.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

@ExtendWith(OutputCaptureExtension.class)
class RequestIdFilterTest {

	private final RequestIdFilter filter = new RequestIdFilter();

	@Test
	void generatesIdAvailableInMdcDuringRequestAndReturnsItInHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> idSeenByHandler = new AtomicReference<>();
		FilterChain chain = (req, res) -> idSeenByHandler.set(MDC.get(RequestIdFilter.MDC_KEY));

		filter.doFilter(request, response, chain);

		assertThat(idSeenByHandler.get()).matches("[0-9a-f-]{36}");
		assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo(idSeenByHandler.get());
		// Não vaza para a próxima requisição atendida pela mesma thread
		assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
	}

	@Test
	void reusesIdSentByProxy() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
		request.addHeader(RequestIdFilter.HEADER, "7f3c9a1b2d4e5f60");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) -> {
		});

		assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("7f3c9a1b2d4e5f60");
	}

	@Test
	void replacesUnsafeIncomingId() {
		assertThat(RequestIdFilter.resolveRequestId("abc\nFAKE LOG LINE")).matches("[0-9a-f-]{36}");
		assertThat(RequestIdFilter.resolveRequestId("short")).matches("[0-9a-f-]{36}");
		assertThat(RequestIdFilter.resolveRequestId(null)).matches("[0-9a-f-]{36}");
	}

	@Test
	void logsMethodPathStatusAndDuration(CapturedOutput output) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/orders/7/status");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(422));

		assertThat(output).containsPattern("PATCH /api/orders/7/status -> 422 \\(\\d+ ms\\)");
	}

	@Test
	void skipsActuatorRequests(CapturedOutput output) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) -> {
		});

		assertThat(response.getHeader(RequestIdFilter.HEADER)).isNull();
		assertThat(output).doesNotContain("/actuator/health");
	}

}
