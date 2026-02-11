package com.datashare.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.datashare.api.entities.File;
import com.datashare.api.entities.Token;
import com.datashare.api.handler.InvalidTokenException;
import com.datashare.api.repository.TokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** TokenService Unit Test Set */
@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

  @Mock TokenRepository tokenRepository;

  @InjectMocks TokenService tokenService;

  /** Test successful token generation */
  @Test
  @DisplayName("UNIT-TOKEN-001: Token generation succeeds")
  public void shouldGenerateTokenSuccessfully() {
    // Arrange
    File file = new File();
    file.setId(1L);
    file.setFilename("test.pdf");
    Instant expiresAt = Instant.now().plusSeconds(86400);

    // Act
    Token token = tokenService.generateToken(file, expiresAt);

    // Assert
    assertNotNull(token.getTokenString());
    assertEquals(6, token.getTokenString().length());
    verify(tokenRepository, times(1)).save(any(Token.class));
  }

  /** Test that generated token has correct length */
  @Test
  @DisplayName("UNIT-TOKEN-002: Generated token has exactly 6 characters")
  public void shouldGenerateTokenWithCorrectLength() {
    // Arrange
    File file = new File();
    file.setId(2L);
    Instant expiresAt = Instant.now().plusSeconds(86400);

    // Act
    Token token = tokenService.generateToken(file, expiresAt);

    // Assert
    assertEquals(6, token.getTokenString().length());
  }

  /** Test that generated token contains only valid characters */
  @Test
  @DisplayName("UNIT-TOKEN-003: Generated token contains only valid characters")
  public void shouldGenerateTokenWithValidCharacters() {
    // Arrange
    File file = new File();
    file.setId(3L);
    Instant expiresAt = Instant.now().plusSeconds(86400);
    String validAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // Act
    Token token = tokenService.generateToken(file, expiresAt);

    // Assert
    for (char c : token.getTokenString().toCharArray()) {
      assertTrue(validAlphabet.contains(String.valueOf(c)), "Invalid character in token: " + c);
    }
  }

  /** Test that generated tokens are random (different) */
  @Test
  @DisplayName("UNIT-TOKEN-004: Generated tokens are randomized")
  public void shouldGenerateDifferentTokens() {
    // Arrange
    File file1 = new File();
    file1.setId(4L);
    File file2 = new File();
    file2.setId(5L);
    Instant expiresAt = Instant.now().plusSeconds(86400);

    // Act
    Token token1 = tokenService.generateToken(file1, expiresAt);
    Token token2 = tokenService.generateToken(file2, expiresAt);

    // Assert
    assertNotEquals(
        token1.getTokenString(),
        token2.getTokenString(),
        "Tokens should be different due to randomization");
  }

  /** Test that token metadata is correctly saved */
  @Test
  @DisplayName("UNIT-TOKEN-005: Token metadata is correctly saved in database")
  public void shouldSaveTokenMetadataCorrectly() {
    // Arrange
    File file = new File();
    file.setId(6L);
    file.setFilename("document.docx");

    Instant expiresAt = Instant.now().plusSeconds(86400);

    // Act
    Token token = tokenService.generateToken(file, expiresAt);

    // Assert
    ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
    verify(tokenRepository, times(1)).save(tokenCaptor.capture());

    Token savedToken = tokenCaptor.getValue();
    assertEquals(token.getTokenString(), savedToken.getTokenString());
    assertEquals(file, savedToken.getFile());
    assertEquals(expiresAt, savedToken.getExpiresAt());
  }

  /** Test successful token validation */
  @Test
  @DisplayName("UNIT-TOKEN-006: Token validation succeeds for valid token")
  public void shouldValidateTokenSuccessfully() throws Exception {
    // Arrange
    String tokenString = "ABC123";
    File file = new File();
    file.setId(7L);
    file.setFilename("test.pdf");

    Token token = new Token();
    token.setTokenString(tokenString);
    token.setFile(file);
    token.setExpiresAt(Instant.now().plusSeconds(86400));
    file.setToken(token);

    when(tokenRepository.findByTokenString(tokenString)).thenReturn(Optional.of(token));

    // Act
    File result = tokenService.validateToken(tokenString);

    // Assert
    assertNotNull(result);
    assertEquals(file, result);
    assertEquals("test.pdf", result.getFilename());
    verify(tokenRepository, times(1)).findByTokenString(tokenString);
  }

  /** Test token validation with non-existent token */
  @Test
  @DisplayName("UNIT-TOKEN-007: Token validation fails for non-existent token")
  public void shouldThrowExceptionForNonExistentToken() {
    // Arrange
    String invalidToken = "INVALID";
    when(tokenRepository.findByTokenString(invalidToken)).thenReturn(Optional.empty());

    // Act & Assert
    InvalidTokenException exception =
        assertThrows(InvalidTokenException.class, () -> tokenService.validateToken(invalidToken));

    assertEquals("Unknown token", exception.getMessage());
    verify(tokenRepository, times(1)).findByTokenString(invalidToken);
  }

  /** Test token validation with expired token */
  @Test
  @DisplayName("UNIT-TOKEN-008: Token validation fails for expired token")
  public void shouldThrowExceptionForExpiredToken() {
    // Arrange
    String tokenString = "EXP123";
    File file = new File();
    file.setId(8L);

    Token token = new Token();
    token.setTokenString(tokenString);
    token.setFile(file);
    // Set expiration to 1 hour ago
    token.setExpiresAt(Instant.now().minusSeconds(3600));
    file.setToken(token);

    when(tokenRepository.findByTokenString(tokenString)).thenReturn(Optional.of(token));

    // Act & Assert
    InvalidTokenException exception =
        assertThrows(InvalidTokenException.class, () -> tokenService.validateToken(tokenString));

    assertEquals("Expired token", exception.getMessage());
    verify(tokenRepository, times(1)).findByTokenString(tokenString);
  }

  /** Test token with upcoming expiration is still valid */
  @Test
  @DisplayName("UNIT-TOKEN-009: Token with future expiration is valid")
  public void shouldValidateTokenWithFutureExpiration() throws Exception {
    // Arrange
    String tokenString = "FUTURE1";
    File file = new File();
    file.setId(9L);
    file.setFilename("future.pdf");

    Token token = new Token();
    token.setTokenString(tokenString);
    token.setFile(file);
    // Set expiration to 24 hours in the future
    token.setExpiresAt(Instant.now().plusSeconds(86400));
    file.setToken(token);

    when(tokenRepository.findByTokenString(tokenString)).thenReturn(Optional.of(token));

    // Act
    File result = tokenService.validateToken(tokenString);

    // Assert
    assertNotNull(result);
    assertEquals(file, result);
  }

  /** Test token validation at expiration boundary */
  @Test
  @DisplayName("UNIT-TOKEN-010: Token validation fails just after expiration time")
  public void shouldFailValidationJustAfterExpiration() {
    // Arrange
    String tokenString = "BOUNDARY";
    File file = new File();
    file.setId(10L);

    Token token = new Token();
    token.setTokenString(tokenString);
    token.setFile(file);
    // Set expiration to 1 second ago
    token.setExpiresAt(Instant.now().minusSeconds(1));
    file.setToken(token);

    when(tokenRepository.findByTokenString(tokenString)).thenReturn(Optional.of(token));

    // Act & Assert
    InvalidTokenException exception =
        assertThrows(InvalidTokenException.class, () -> tokenService.validateToken(tokenString));

    assertEquals("Expired token", exception.getMessage());
  }

  /** Test multiple token generations for same file */
  @Test
  @DisplayName("UNIT-TOKEN-011: Multiple tokens can be generated for the same file")
  public void shouldAllowMultipleTokensPerFile() {
    // Arrange
    File file = new File();
    file.setId(11L);
    file.setFilename("shared.pdf");

    Instant expiresAt1 = Instant.now().plusSeconds(86400);
    Instant expiresAt2 = Instant.now().plusSeconds(172800);

    // Act
    Token token1 = tokenService.generateToken(file, expiresAt1);
    Token token2 = tokenService.generateToken(file, expiresAt2);

    // Assert
    assertNotNull(token1.getTokenString());
    assertNotNull(token2.getTokenString());
    assertNotEquals(token1.getTokenString(), token2.getTokenString());
    verify(tokenRepository, times(2)).save(any(Token.class));
  }

  /** Test that token service correctly handles different expiration times */
  @Test
  @DisplayName("UNIT-TOKEN-012: Token service preserves correct expiration time")
  public void shouldPreserveCorrectExpirationTime() {
    // Arrange
    File file = new File();
    file.setId(12L);
    Instant expiresAt = Instant.parse("2026-02-16T10:30:00Z");

    // Act
    tokenService.generateToken(file, expiresAt);

    // Assert
    ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
    verify(tokenRepository, times(1)).save(tokenCaptor.capture());

    Token savedToken = tokenCaptor.getValue();
    assertEquals(expiresAt, savedToken.getExpiresAt());
  }
}
