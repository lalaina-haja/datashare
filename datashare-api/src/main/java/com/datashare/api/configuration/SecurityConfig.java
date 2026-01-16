package com.datashare.api.configuration;

import com.datashare.api.service.security.CustomUserDetailService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration for Spring Security.
 *
 * <p>This class defines the security filter chain for the application, configuring CSRF protection,
 * authorization rules, and OAuth2 JWT resource server.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Autowired private CustomUserDetailService customUserDetailService;

  @Value("${jwt.secret}")
  private String secret;

  private SecretKeySpec secretKey;

  /**
   * Initializes and validates the JWT secret key.
   *
   * <p>Verifies that the secret is at least 32 characters (256 bits) and creates a SecretKeySpec
   * for HMAC SHA-256 signing.
   *
   * @throws IllegalStateException if the secret is less than 256 bits
   */
  @PostConstruct
  @SuppressWarnings("unused")
  void init() {
    if (secret.length() < 32) {
      throw new IllegalStateException("JWT secret must be at least 256 bits");
    }
    this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }

  /**
   * Creates a JwtEncoder bean for token generation.
   *
   * @return a NimbusJwtEncoder configured with the secret key
   */
  @Bean
  @SuppressWarnings("unused")
  JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
  }

  /**
   * Creates a JwtDecoder bean for token validation.
   *
   * @return a NimbusJwtDecoder configured with the secret key
   */
  @Bean
  @SuppressWarnings("unused")
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(secretKey).build();
  }

  @Bean
  /**
   * Provides an AuthenticationProvider that delegates to the application's {@link
   * com.datashare.api.service.security.CustomUserDetailService} and uses the configured password
   * encoder.
   *
   * @return a configured {@link AuthenticationProvider} bean
   */
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Exposes the framework-provided {@link AuthenticationManager} so it can be injected elsewhere in
   * the application (for example in authentication endpoints).
   *
   * @param config the {@link AuthenticationConfiguration}
   * @return the application {@link AuthenticationManager}
   * @throws Exception if the authentication manager cannot be obtained
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configures the security filter chain for HTTP requests.
   *
   * <p>Permits public access to authentication endpoints, health/info actuators, and public files.
   * All other requests require authentication via JWT.
   *
   * @param http the HttpSecurity object to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  @SuppressWarnings("unused")
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/auth/**", "/actuator/health", "/actuator/info", "/files/public/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }

  /**
   * Password encoder bean using BCrypt hashing algorithm.
   *
   * @return a {@link PasswordEncoder} instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
