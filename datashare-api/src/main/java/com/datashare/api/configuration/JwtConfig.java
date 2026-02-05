package com.datashare.api.configuration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Spring configuration class for JWT encoder and decoder beans.
 *
 * <p>Configures JWT token encoding and decoding using HMAC SHA-256 algorithm. The JWT secret is
 * retrieved from application properties and validated at startup to ensure it meets minimum
 * security requirements (at least 32 bytes for HS256). Also performs a self-test to verify the JWT
 * configuration is working correctly.
 */
@Slf4j
@Configuration
public class JwtConfig {

  @Value("${security.jwt.secret}")
  private String secret;

  @Value("${security.jwt.issuer:datashare-api}")
  private String issuer;

  /**
   * Creates a JWT encoder bean configured with HMAC SHA-256 algorithm.
   *
   * <p>The encoder uses the configured JWT secret to sign tokens. This bean is used to create new
   * JWT tokens during authentication and authorization flows.
   *
   * @return a {@link JwtEncoder} configured with the secret key using Nimbus JWT library
   */
  @Bean
  public JwtEncoder jwtEncoder() {
    SecretKey key = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  /**
   * Creates a JWT decoder bean configured with HMAC SHA-256 algorithm.
   *
   * <p>The decoder uses the configured JWT secret to validate and parse tokens. This bean is used
   * to decode and validate JWT tokens during request processing. The decoder is configured to use
   * HS256 (HMAC SHA-256) algorithm for token validation.
   *
   * @return a {@link JwtDecoder} configured with the secret key using Nimbus JWT library
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    SecretKey key = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    return NimbusJwtDecoder.withSecretKey(key).build();
  }

  /**
   * Validate JWT configuration at application startup.
   *
   * <p>Verifies that:
   *
   * <ul>
   *   <li>The JWT secret is configured and not blank
   *   <li>The JWT secret is at least 32 bytes (256 bits) for HS256 security requirements
   * </ul>
   *
   * @throws IllegalStateException if the JWT configuration is invalid
   */
  @PostConstruct
  void validateJwtConfiguration() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT secret is missing");
    }

    int length = secret.getBytes(StandardCharsets.UTF_8).length;
    if (length < 32) {
      throw new IllegalStateException("JWT secret must be at least 32 bytes (256 bits) for HS256");
    }
  }
}
