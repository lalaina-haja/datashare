package com.datashare.api.configuration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Configuration for JWT encoding and decoding.
 *
 * <p>This class sets up the JWT encoder and decoder beans using HMAC SHA-256 encryption with a
 * secret key. Validates that the secret key is at least 256 bits.
 */
@Configuration
public class JwtConfig {

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
  JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
  }

  /**
   * Creates a JwtDecoder bean for token validation.
   *
   * @return a NimbusJwtDecoder configured with the secret key
   */
  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(secretKey).build();
  }
}
