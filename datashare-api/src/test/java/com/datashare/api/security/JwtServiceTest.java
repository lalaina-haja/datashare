package com.datashare.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;

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
  private UserDetails testUser;

  private static final String EMAIL = "e@mail.com";
  private static final String PASSWORD = "PASSWORD";
  private static final long EXPIRATION_SECONDS = 3600L;
  private static final String ISSUER = "datashare-api";
  private static final String TOKEN = "TOKEN";

  @BeforeEach
  public void setUp() {
    jwtService = new JwtService(jwtEncoder, jwtDecoder, EXPIRATION_SECONDS, ISSUER);
    testUser =
        org.springframework.security.core.userdetails.User.builder()
            .username(EMAIL)
            .password(PASSWORD)
            .build();
  }

  /** Tests successful JWT token generation */
  @Test
  @DisplayName("UNIT-JWT-001: Should generate token successfully")
  public void get_token_successful() {

    // GIVEN the user details successfully authenticated
    Instant expiresAt = Instant.now().plusSeconds(EXPIRATION_SECONDS);
    when(jwt.getTokenValue()).thenReturn(TOKEN);
    when(jwt.getExpiresAt()).thenReturn(expiresAt);
    when(jwt.getSubject()).thenReturn(EMAIL);
    when(jwtEncoder.encode(any())).thenReturn(jwt);
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN generating a token
    String returnedToken = jwtService.generateToken(testUser);
    Instant returnedExpiresAt = jwtService.getExpiresAt(returnedToken);
    Long returnedExpiresIn = jwtService.getExpiresIn(returnedToken);
    String subject = jwtService.extractUsername(returnedToken);

    // THEN the token is returned successfully
    assertThat(returnedToken).isEqualTo(TOKEN);
    assertThat(returnedExpiresAt).isEqualTo(expiresAt);
    assertTrue(Math.abs(EXPIRATION_SECONDS - returnedExpiresIn) <= 1);
    assertThat(subject).isEqualTo(EMAIL);
  }

  /** Test that an invalid user throws Exception for Encoding failed */
  @Test
  @DisplayName("UNIT-JWT-002: Should throw exception for encoding failed on invalid user")
  public void get_token_invalid_user_details_throws_exception() {

    // GIVEN invalid user details
    when(jwtEncoder.encode(any())).thenThrow(new RuntimeException("Encoding failed"));

    // WHEN generating a token
    Exception exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class, () -> jwtService.generateToken(testUser));

    // THEN expect exception thrown with message "Encoding failed"
    assertThat(exception.getMessage()).isEqualTo("Encoding failed");
  }

  /** Test that an expired token is invalid */
  @Test
  @DisplayName("UNIT-JWT-003: Should returns invalid for an expired token")
  public void expired_token_is_invalid() {

    // GIVEN an expired token
    Instant pastTime = Instant.now().minusSeconds(3600);
    when(jwt.getExpiresAt()).thenReturn(pastTime);
    when(jwt.getSubject()).thenReturn(EMAIL);
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN validating the token
    String token = "EXPIRED_TOKEN";
    boolean isValid = jwtService.isTokenValid(token, testUser);
    boolean isExpired = jwtService.isTokenExpired(token);

    // THEN the token is invalid and expired
    assertThat(isExpired).isTrue();
    assertThat(isValid).isFalse();
  }

  /** Test that isValid() returns true for a valid token */
  @Test
  @DisplayName("UNIT-JWT-004: Should return valid for valid a token")
  public void valid_token_is_valid() {

    // GIVEN a valid token
    Instant futureTime = Instant.now().plusSeconds(3600);
    when(jwt.getExpiresAt()).thenReturn(futureTime);
    when(jwt.getSubject()).thenReturn(EMAIL);
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN validating the token
    String token = "VALID_TOKEN";
    boolean isValid = jwtService.isTokenValid(token, testUser);
    boolean isExpired = jwtService.isTokenExpired(token);

    // THEN the token is valid and not expired
    assertThat(isExpired).isFalse();
    assertThat(isValid).isTrue();
  }

  /** Test that a token with wrong user is invalid */
  @Test
  @DisplayName("UNIT-JWT-005: Should return invalid for wrong user")
  public void token_with_wrong_user_is_invalid() {

    // GIVEN a token with a different subject
    Instant futureTime = Instant.now().plusSeconds(3600);
    when(jwt.getExpiresAt()).thenReturn(futureTime);
    when(jwt.getSubject()).thenReturn("WRONG_USER");
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // WHEN validating the token
    String token = "TOKEN_WITH_WRONG_USER";
    boolean isValid = jwtService.isTokenValid(token, testUser);

    // THEN the token is invalid
    assertThat(isValid).isFalse();
  }

  /** Test that the email is exctracted from the token */
  @Test
  @DisplayName("UNIT-JWT-006: Should extract email from token")
  void testExtractUsername() {

    // GIVEN the user details successfully authenticated
    when(jwt.getTokenValue()).thenReturn(TOKEN);
    when(jwt.getSubject()).thenReturn(EMAIL);
    when(jwtEncoder.encode(any())).thenReturn(jwt);
    when(jwtDecoder.decode(any())).thenReturn(jwt);

    // AND the token
    String token = jwtService.generateToken(testUser);

    // WHEN extracting the username THEN got email
    String username = jwtService.extractUsername(token);
    assertThat(username).isEqualTo(EMAIL);
  }

  /** Test that getExpiresAt returns expiration when token valid */
  @Test
  @DisplayName("UNIT-JWT-007: Should returns expiration")
  void getExpiresAt_shouldReturnExpiration_whenTokenValid() {

    Instant exp = Instant.now().plusSeconds(3600);
    Jwt jwt = mock(Jwt.class);
    when(jwt.getExpiresAt()).thenReturn(exp);
    when(jwtDecoder.decode("valid")).thenReturn(jwt);

    assertEquals(exp, jwtService.getExpiresAt("valid"));
  }

  /** Test that getExpiredAt returns null when token invalid */
  @Test
  @DisplayName("UNIT-JWT-008: Should expiration null when token invalid")
  void getExpiresAt_shouldReturnNull_whenTokenInvalid() {

    when(jwtDecoder.decode("invalid")).thenThrow(new JwtException("bad token"));

    assertNull(jwtService.getExpiresAt("invalid"));
  }

  /** Test that getExpiresIn returns positive value when token valid */
  @Test
  @DisplayName("UNIT-JWT-009: Should return positive expiresIn when token valid")
  void getExpiresIn_shouldReturnPositiveValue_whenTokenValid() {

    Instant exp = Instant.now().plusSeconds(120);
    Jwt jwt = mock(Jwt.class);
    when(jwt.getExpiresAt()).thenReturn(exp);
    when(jwtDecoder.decode("valid")).thenReturn(jwt);

    Long result = jwtService.getExpiresIn("valid");
    assertTrue(result > 0);
  }

  /** Test that getExpiresIn returns zero when token invalid */
  @Test
  @DisplayName("UNIT-JWT-010: Should return zero expiresIn when token invalid")
  void getExpiresIn_shouldReturnZero_whenTokenInvalid() {

    when(jwtDecoder.decode("invalid")).thenThrow(new JwtException("bad token"));

    assertEquals(0L, jwtService.getExpiresIn("invalid"));
  }

  /** Test that isTokenExpired returns true when token is expired */
  @Test
  @DisplayName("UNIT-JWT-011: Should return true with isTokenExpired when token expired")
  void isTokenExpired_shouldReturnTrue_whenExpired() {

    Instant exp = Instant.now().minusSeconds(10);
    Jwt jwt = mock(Jwt.class);
    when(jwt.getExpiresAt()).thenReturn(exp);
    when(jwtDecoder.decode("expired")).thenReturn(jwt);

    assertTrue(jwtService.isTokenExpired("expired"));
  }

  /** Test that isTokenExpired returns false when token is not expired */
  @Test
  @DisplayName("UNIT-JWT-012: Should return false with isTokenExpired when token not expired")
  void isTokenExpired_shouldReturnFalse_whenNotExpired() {

    Instant exp = Instant.now().plusSeconds(3600);
    Jwt jwt = mock(Jwt.class);
    when(jwt.getExpiresAt()).thenReturn(exp);
    when(jwtDecoder.decode("valid")).thenReturn(jwt);

    assertFalse(jwtService.isTokenExpired("valid"));
  }

  /** Test that isTokenExpired returns true when token is invalid */
  @Test
  @DisplayName("UNIT-JWT-013: Should return true with isTokenExpired when token is invalid")
  void isTokenExpired_shouldReturnTrue_whenTokenInvalid() {

    when(jwtDecoder.decode("invalid")).thenThrow(new JwtException("bad token"));

    assertTrue(jwtService.isTokenExpired("invalid"));
  }
}
