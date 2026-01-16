package com.datashare.api.service;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Service for generating JWT tokens.
 *
 * <p>This service provides functionality to create JWT tokens with customizable expiration times
 * and issuer information. Tokens are signed using the configured JWT encoder.
 */
@Service
public class JwtService {

  private final JwtEncoder jwtEncoder;
  private final long expirationSeconds;
  private final String issuer;

  /**
   * Constructs a JwtService with the specified encoder and configuration.
   *
   * @param jwtEncoder the JWT encoder used to encode tokens
   * @param expirationSeconds the token expiration time in seconds
   * @param issuer the issuer of the token (typically the application name)
   */
  public JwtService(
      JwtEncoder jwtEncoder,
      @Value("${jwt.expiration}") long expirationSeconds,
      @Value("${spring.application.name}") String issuer) {
    this.jwtEncoder = jwtEncoder;
    this.expirationSeconds = expirationSeconds;
    this.issuer = issuer;
  }

  /**
   * Generates a JWT token for the specified user.
   *
   * <p>The token includes the user ID as the subject, the current timestamp, and an expiration time
   * based on the configured expiration duration.
   *
   * @param userId the unique identifier of the user
   * @return the encoded JWT token as a string
   */
  public String generateToken(String userId) {

    Instant now = Instant.now();

    JwtClaimsSet claimsSet =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expirationSeconds))
            .subject(userId)
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }
}
