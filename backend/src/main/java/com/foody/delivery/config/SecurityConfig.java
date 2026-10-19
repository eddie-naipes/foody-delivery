package com.foody.delivery.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.foody.delivery.repository.UserRepository;
import com.foody.delivery.security.JwtAuthenticationEntryPoint;
import com.foody.delivery.security.JwtAuthenticationFilter;
import com.foody.delivery.service.JwtService;
import com.foody.delivery.util.Messages;

/**
 * Segurança stateless: sem sessão e sem CSRF (não há cookies); a identidade vem do
 * token JWT em cada requisição. Públicos: autenticação, a documentação da API e o
 * health check. Todo o resto exige token.
 */
@Configuration
public class SecurityConfig {

	static final String[] PUBLIC_PATHS = { "/api/auth/**", "/error", "/swagger-ui.html", "/swagger-ui/**",
			"/v3/api-docs/**", "/actuator/health", "/actuator/info" };

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
			UserRepository userRepository, JwtAuthenticationEntryPoint entryPoint) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_PATHS)
				.permitAll()
				.anyRequest()
				.authenticated())
			.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
			// Instanciado aqui (e não como @Component) para não ser registrado duas vezes como filtro do servlet
			.addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository),
					UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return email -> userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException(Messages.USER_NOT_FOUND));
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
		config.setExposedHeaders(List.of(HttpHeaders.LOCATION));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

}
