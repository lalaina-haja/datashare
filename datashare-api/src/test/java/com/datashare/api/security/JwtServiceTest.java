package com.datashare.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.datashare.api.service.security.JwtService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

/**
 * Unit tests for the JwtService class.
 *
 * <p>This test class verifies the JWT token generation functionality of the JwtService. It uses
 * Mockito to mock the JwtEncoder dependency and validates that tokens are generated correctly with
 * the expected user ID.
 */
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

  @Mock private JwtEncoder jwtEncoder;
  @Mock private JwtDecoder jwtDecoder;
  @Mock private Jwt jwt;

  private JwtService jwtService;

  private static final String EMAIL = "e@mail.com";
  private static final String PASSWORD = "PASSWORD";
  private static final long EXPIRATION_SECONDS = 3600L;
  private static final String ISSUER = "datashare-api";
  private static final String TOKEN = "TOKEN";

  @BeforeEach
  public void setUp() {
    jwtService = new JwtService(jwtEncoder, jwtDecoder, EXPIRATION_SECONDS, ISSUER);
  }

  /**
   * Tests successful JWT token generation.
   *
   * <p>Verifies that when a user ID is provided, the JwtService correctly generates and returns a
   * JWT token with the expected content.
   */
  @Test
  public void get_token_successful() {

    // GIVEN the user details successfully authenticated
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.builder()
            .username(EMAIL)
            .password(PASSWORD)
            .build();
    Instant expiresAt = Instant.now().plusSeconds(EXPIRATION_SECONDS);
    when(jwt.getTokenValue()).thenReturn(TOKEN);
    when(jwt.getExpiresAt()).thenReturn(expiresAt);
    when(jwt.getSubject()).thenReturn(EMAIL);
    when(jwtEncoder.encode(any())).thenReturn(jwt);
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN generating a token
    String returnedToken = jwtService.generateToken(userDetails);
    Instant returnedExpiresAt = jwtService.getExpiresAt(returnedToken);
    Long returnedExpiresIn = jwtService.getExpiresIn(returnedToken);
    String subject = jwtService.extractUsername(returnedToken);

    // THEN the token is returned successfully
    assertThat(returnedToken).isEqualTo(TOKEN);
    assertThat(returnedExpiresAt).isEqualTo(expiresAt);
    assertTrue(Math.abs(EXPIRATION_SECONDS - returnedExpiresIn) <= 1);
    assertThat(subject).isEqualTo(EMAIL);
  }

  @Test
  public void get_token_invalid_user_details_throws_exception() {

    // GIVEN invalid user details
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.builder()
            .username(EMAIL)
            .password(PASSWORD)
            .build();
    when(jwtEncoder.encode(any())).thenThrow(new RuntimeException("Encoding failed"));

    // WHEN generating a token
    Exception exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class, () -> jwtService.generateToken(userDetails));

    // THEN expect exception thrown with message "Encoding failed"
    assertThat(exception.getMessage()).isEqualTo("Encoding failed");
  }

  @Test
  public void expired_token_is_invalid() {

    // GIVEN an expired token
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.builder()
            .username(EMAIL)
            .password(PASSWORD)
            .build();
    Instant pastTime = Instant.now().minusSeconds(3600);
    when(jwt.getExpiresAt()).thenReturn(pastTime);
    when(jwt.getSubject()).thenReturn(EMAIL);
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN validating the token
    String token = "EXPIRED_TOKEN";
    boolean isValid = jwtService.isTokenValid(token, userDetails);
    boolean isExpired = jwtService.isTokenExpired(token);

    // THEN the token is invalid and expired
    assertThat(isExpired).isTrue();
    assertThat(isValid).isFalse();
  }

  @Test
  public void valid_token_is_valid() {

    // GIVEN a valid token
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.builder()
            .username(EMAIL)
            .password(PASSWORD)
            .build();
    Instant futureTime = Instant.now().plusSeconds(3600);
    when(jwt.getExpiresAt()).thenReturn(futureTime);
    when(jwt.getSubject()).thenReturn(EMAIL);
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN validating the token
    String token = "VALID_TOKEN";
    boolean isValid = jwtService.isTokenValid(token, userDetails);
    boolean isExpired = jwtService.isTokenExpired(token);

    // THEN the token is valid and not expired
    assertThat(isExpired).isFalse();
    assertThat(isValid).isTrue();
  }

  @Test
  public void token_with_wrong_user_is_invalid() {

    // GIVEN a token with a different subject
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.builder()
            .username(EMAIL)
            .password(PASSWORD)
            .build();
    Instant futureTime = Instant.now().plusSeconds(3600);
    when(jwt.getExpiresAt()).thenReturn(futureTime);
    when(jwt.getSubject()).thenReturn("WRONG_USER");
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN validating the token
    String token = "TOKEN_WITH_WRONG_USER";
    boolean isValid = jwtService.isTokenValid(token, userDetails);

    // THEN the token is invalid
    assertThat(isValid).isFalse();
  }
}
