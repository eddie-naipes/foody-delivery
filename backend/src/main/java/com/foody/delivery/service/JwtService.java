package com.foody.delivery.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.foody.delivery.model.User;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Gera e valida tokens JWT (HS256). O subject do token é o e-mail do usuário.
 */
@Service
public class JwtService {

	private final SecretKey key;

	private final Duration expiration;

	private final Clock clock;

	private final JwtParser parser;

	public JwtService(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration}") Duration expiration, Clock clock) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.expiration = expiration;
		this.clock = clock;
		this.parser = Jwts.parser().verifyWith(key).clock(() -> Date.from(clock.instant())).build();
	}

	public String generateToken(User user) {
		Instant now = clock.instant();
		return Jwts.builder()
			.subject(user.getEmail())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plus(expiration)))
			.signWith(key)
			.compact();
	}

	/**
	 * Retorna o e-mail contido no token, ou vazio se o token for inválido, expirado
	 * ou assinado com outra chave.
	 */
	public Optional<String> extractValidSubject(String token) {
		try {
			return Optional.of(parser.parseSignedClaims(token).getPayload().getSubject());
		}
		catch (JwtException | IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

}
