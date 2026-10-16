package com.foody.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.foody.delivery.model.User;

class JwtServiceTest {

	private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdHMtMTIzNDU2Nzg5MA==";

	private static final String OTHER_SECRET = "b3RoZXItc2VjcmV0LWtleS1mb3Itand0LXRlc3RzLTEyMzQ1Njc4OTA=";

	private static final Duration EXPIRATION = Duration.ofHours(8);

	private final User user = new User("Maria", "maria@foody.com", "hash");

	private final JwtService jwtService = new JwtService(SECRET, EXPIRATION, Clock.systemUTC());

	@Test
	void generatedTokenReturnsEmailAsSubject() {
		String token = jwtService.generateToken(user);

		assertThat(jwtService.extractValidSubject(token)).contains("maria@foody.com");
	}

	@Test
	void expiredTokenReturnsEmpty() {
		Clock nineHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(9)), ZoneOffset.UTC);
		String expired = new JwtService(SECRET, EXPIRATION, nineHoursAgo).generateToken(user);

		assertThat(jwtService.extractValidSubject(expired)).isEmpty();
	}

	@Test
	void tokenSignedWithOtherSecretReturnsEmpty() {
		String foreign = new JwtService(OTHER_SECRET, EXPIRATION, Clock.systemUTC()).generateToken(user);

		assertThat(jwtService.extractValidSubject(foreign)).isEmpty();
	}

	@Test
	void malformedTokenReturnsEmpty() {
		assertThat(jwtService.extractValidSubject("abc")).isEmpty();
	}

	@Test
	void blankTokenReturnsEmpty() {
		assertThat(jwtService.extractValidSubject("")).isEmpty();
	}

}
