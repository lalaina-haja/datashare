package com.datashare.api.security;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/**
 * Service for generating JWT tokens.
 *
 * <p>This service provides functionality to create JWT tokens with customizable expiration times
 * and issuer information. Tokens are signed using the configured JWT encoder.
 */
@Service
@Slf4j
public class JwtService {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final long expirationSeconds;
  private final String issuer;

  /**
   * Create a new JwtService.
   *
   * @param jwtEncoder encoder used to sign tokens
   * @param jwtDecoder decoder used to parse and validate tokens
   * @param expirationSeconds token lifetime in seconds
   * @param issuer token issuer (typically the application name)
   */
  public JwtService(
      JwtEncoder jwtEncoder,
      JwtDecoder jwtDecoder,
      @Value("${security.jwt.expiration}") long expirationSeconds,
      @Value("${security.jwt.issuer}") String issuer) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.expirationSeconds = expirationSeconds;
    this.issuer = issuer;
    log.info("jwtService created with: " + expirationSeconds + ", " + issuer);
  }

  /**
   * Generate a signed JWT for the given user details.
   *
   * <p>The token will include standard claims such as issuer, issuedAt, expiresAt and subject
   * (username).
   *
   * @param userDetails the authenticated user
   * @return a signed JWT token string
   */
  public String generateToken(UserDetails userDetails) {

    Instant now = Instant.now();

    JwtClaimsSet claimsSet =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expirationSeconds))
            .subject(userDetails.getUsername())
            .build();

    Jwt jwt =
        jwtEncoder.encode(
            JwtEncoderParameters.from(JwsHeader.with(() -> "HS256").build(), claimsSet));

    return jwt.getTokenValue();
  }

  /**
   * Validate the provided JWT against the given user details.
   *
   * <p>This verifies that the token is not expired and that the subject matches the provided user's
   * username.
   *
   * @param token the JWT string to validate
   * @param userDetails the expected user details
   * @return true if the token is valid and belongs to the user
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      Instant exp = jwt.getExpiresAt();
      String subject = jwt.getSubject();

      boolean notExpired = exp != null && exp.isAfter(Instant.now());
      boolean correctUser = subject.equals(userDetails.getUsername());

      return notExpired && correctUser;
    } catch (JwtException e) {
      log.debug("Invalid token: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Get the expiration time of the JWT in seconds from now.
   *
   * @param token the JWT string
   * @return the number of seconds until expiration, or "0" if expired or invalid
   */
  public Instant getExpiresAt(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      return jwt.getExpiresAt();
    } catch (JwtException e) {
      log.debug("Invalid token: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Get the expiration duration in seconds.
   *
   * @return the expiration duration in seconds
   */
  public Long getExpiresIn(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      Instant exp = jwt.getExpiresAt();
      return (exp != null)
          ? Math.max(0, java.time.Duration.between(Instant.now(), exp).getSeconds())
          : 0L;
    } catch (JwtException e) {
      log.debug("Invalid token: {}", e.getMessage());
      return 0L;
    }
  }

  /**
   * Check whether the given JWT is expired.
   *
   * @param token the JWT string to inspect
   * @return true if the token is expired or cannot be decoded
   */
  public boolean isTokenExpired(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      Instant exp = jwt.getExpiresAt();
      return exp != null && exp.isBefore(Instant.now());
    } catch (JwtException e) {
      return true;
    }
  }

  /**
   * Extract the username (subject) from the JWT.
   *
   * @param token the JWT string
   * @return the subject (username) if present, otherwise null
   */
  public String extractUsername(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      return jwt.getSubject();
    } catch (JwtException e) {
      return null;
    }
  }
}
