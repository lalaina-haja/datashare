package com.datashare.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.datashare.api.configuration.S3Properties;
import com.datashare.api.dto.PresignedDownloadResponse;
import com.datashare.api.dto.PresignedUploadResponse;
import com.datashare.api.entities.File;
import com.datashare.api.entities.Token;
import com.datashare.api.entities.User;
import com.datashare.api.handler.InvalidTokenException;
import com.datashare.api.handler.UserNotFileOwnerException;
import com.datashare.api.repository.FileRepository;
import com.datashare.api.repository.TokenRepository;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/** FileService Unit Test Set */
@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

  @Mock S3Presigner presigner;

  @Mock S3Properties s3Properties;

  @Mock FileRepository fileRepository;

  @Mock TokenRepository tokenRepository;

  @Mock TokenService tokenService;

  @Mock Authentication authentication;

  @InjectMocks FileService fileService;

  /** Test that file size exceeding 1GB throws exception */
  @Test
  @DisplayName("UNIT-FILE-001: File size exceeds 1GB")
  public void shouldRejectFileLargerThan1GB() {
    assertThrows(
        IllegalArgumentException.class,
        () -> fileService.createUploadUrl("test.png", "image/png", 1_000_000_001L, null, null));
  }

  /** Test that forbidden file extension throws exception */
  @Test
  @DisplayName("UNIT-FILE-002: File extension rejected")
  public void shouldRejectForbiddenExtensions() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            fileService.createUploadUrl("virus.exe", "application/octet-stream", 1000, null, null));
  }

  /** Test that .bat extension is rejected */
  @Test
  @DisplayName("UNIT-FILE-003: .bat extension rejected")
  public void shouldRejectBatExtension() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            fileService.createUploadUrl(
                "script.bat", "application/octet-stream", 1000, null, null));
  }

  /** Test that .sh extension is rejected */
  @Test
  @DisplayName("UNIT-FILE-004: .sh extension rejected")
  public void shouldRejectShExtension() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            fileService.createUploadUrl("script.sh", "application/octet-stream", 1000, null, null));
  }

  /** Test successful upload URL creation */
  @Test
  @DisplayName("UNIT-FILE-005: Successful upload URL creation")
  public void shouldCreateUploadUrlSuccessfully() throws Exception {
    // Arrange
    String filename = "test.pdf";
    String contentType = "application/pdf";
    long size = 5_000_000L;
    Integer expirationDays = 7;
    Long userId = 1L;

    when(s3Properties.getBucket()).thenReturn("test-bucket");

    PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
    URL mockUrl =
        new URI("https://s3.amazonaws.com/test-bucket/uploads/test-uuid-test.pdf").toURL();
    when(mockPresignedRequest.url()).thenReturn(mockUrl);
    when(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenReturn(mockPresignedRequest);

    Token token = new Token();
    token.setExpiresAt(Instant.now());
    token.setTokenString("TOKEN123");
    when(tokenService.generateToken(any(File.class), any(Instant.class))).thenReturn(token);

    // Act
    PresignedUploadResponse response =
        fileService.createUploadUrl(filename, contentType, size, expirationDays, userId);

    // Assert
    assertNotNull(response);
    assertEquals(
        "https://s3.amazonaws.com/test-bucket/uploads/test-uuid-test.pdf", response.getUploadUrl());
    assertEquals("TOKEN123", response.getTokenString());
    assertNotNull(response.getExpiresAt());

    // Verify file was saved
    verify(fileRepository, times(1)).save(any(File.class));

    // Verify token was generated
    verify(tokenService, times(1)).generateToken(any(File.class), any(Instant.class));
  }

  /** Test successful upload URL creation with default expiration */
  @Test
  @DisplayName("UNIT-FILE-006: Upload URL creation with default 7-day expiration")
  public void shouldCreateUploadUrlWithDefaultExpiration() throws Exception {
    // Arrange
    String filename = "document.docx";
    String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    long size = 2_000_000L;
    Long userId = 2L;

    when(s3Properties.getBucket()).thenReturn("test-bucket");

    PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
    URL mockUrl =
        new URI("https://s3.amazonaws.com/test-bucket/uploads/test-uuid-document.docx").toURL();
    when(mockPresignedRequest.url()).thenReturn(mockUrl);
    when(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenReturn(mockPresignedRequest);

    Token token = new Token();
    token.setTokenString("TOKEN456");
    token.setExpiresAt(Instant.now());
    when(tokenService.generateToken(any(File.class), any(Instant.class))).thenReturn(token);

    // Act
    PresignedUploadResponse response =
        fileService.createUploadUrl(filename, contentType, size, null, userId);

    // Assert
    assertNotNull(response);
    assertNotNull(response.getExpiresAt());
    assertEquals("TOKEN456", response.getTokenString());
  }

  /** Test upload URL creation with maximum allowed file size */
  @Test
  @DisplayName("UNIT-FILE-007: Upload with maximum file size (1GB)")
  public void shouldAcceptMaximumFileSize() throws Exception {
    // Arrange
    String filename = "large.zip";
    String contentType = "application/zip";
    long maxSize = 1_000_000_000L;
    Long userId = 3L;

    when(s3Properties.getBucket()).thenReturn("test-bucket");

    PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
    URL mockUrl =
        new URI("https://s3.amazonaws.com/test-bucket/uploads/test-uuid-large.zip").toURL();
    when(mockPresignedRequest.url()).thenReturn(mockUrl);
    when(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenReturn(mockPresignedRequest);

    Token token = new Token();
    token.setTokenString("TOKEN123");
    token.setExpiresAt(Instant.now());
    when(tokenService.generateToken(any(File.class), any(Instant.class))).thenReturn(token);

    // Act
    PresignedUploadResponse response =
        fileService.createUploadUrl(filename, contentType, maxSize, null, userId);

    // Assert
    assertNotNull(response);
    assertEquals(
        "https://s3.amazonaws.com/test-bucket/uploads/test-uuid-large.zip",
        response.getUploadUrl());
  }

  /** Test successful download URL creation */
  @Test
  @DisplayName("UNIT-FILE-008: Successful download URL creation")
  public void shouldCreateDownloadUrlSuccessfully() throws Exception {
    // Arrange
    String tokenString = "TOKEN123";

    File mockFile = new File();
    mockFile.setId(1L);
    mockFile.setFilename("document.pdf");
    mockFile.setContentType("application/pdf");
    mockFile.setSize(5_000_000L);
    mockFile.setS3Key("uploads/uuid-document.pdf");
    mockFile.setCreatedAt(Instant.now());

    Token mockToken = new Token();
    mockToken.setTokenString(tokenString);
    mockToken.setExpiresAt(Instant.now().plusSeconds(86400));
    mockToken.setFile(mockFile);
    mockFile.setToken(mockToken);

    when(tokenService.validateToken(tokenString)).thenReturn(mockFile);
    when(s3Properties.getBucket()).thenReturn("test-bucket");

    PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
    URL mockUrl = new URI("https://s3.amazonaws.com/test-bucket/uploads/uuid-document.pdf").toURL();
    when(mockPresignedRequest.url()).thenReturn(mockUrl);
    when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(mockPresignedRequest);

    // Act
    PresignedDownloadResponse response = fileService.createDownloadUrl(tokenString);

    // Assert
    assertNotNull(response);
    assertEquals("document.pdf", response.getFilename());
    assertEquals("application/pdf", response.getContentType());
    assertEquals(5_000_000L, response.getSize());
    assertEquals(
        "https://s3.amazonaws.com/test-bucket/uploads/uuid-document.pdf",
        response.getDownloadUrl());

    // Verify token validation was called
    verify(tokenService, times(1)).validateToken(tokenString);
  }

  /** Test download URL creation with invalid token */
  @Test
  @DisplayName("UNIT-FILE-009: Download with invalid token")
  public void shouldThrowExceptionForInvalidToken() throws Exception {
    // Arrange
    String invalidToken = "INVALID";
    when(tokenService.validateToken(invalidToken))
        .thenThrow(new InvalidTokenException("Unknown token"));

    // Act & Assert
    assertThrows(InvalidTokenException.class, () -> fileService.createDownloadUrl(invalidToken));
    verify(tokenService, times(1)).validateToken(invalidToken);
  }

  /** Test download URL creation with expired token */
  @Test
  @DisplayName("UNIT-FILE-010: Download with expired token")
  public void shouldThrowExceptionForExpiredToken() throws Exception {
    // Arrange
    String expiredToken = "EXPIRED";
    when(tokenService.validateToken(expiredToken))
        .thenThrow(new InvalidTokenException("Expired token"));

    // Act & Assert
    assertThrows(InvalidTokenException.class, () -> fileService.createDownloadUrl(expiredToken));
    verify(tokenService, times(1)).validateToken(expiredToken);
  }

  /** Test file metadata is correctly saved */
  @Test
  @DisplayName("UNIT-FILE-011: File metadata is correctly saved in database")
  public void shouldSaveFileMetadataCorrectly() throws Exception {
    // Arrange
    String filename = "test.txt";
    String contentType = "text/plain";
    long size = 1_000L;
    Long userId = 5L;

    when(s3Properties.getBucket()).thenReturn("test-bucket");

    PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
    URL mockUrl =
        new URI("https://s3.amazonaws.com/test-bucket/uploads/test-uuid-test.txt").toURL();
    when(mockPresignedRequest.url()).thenReturn(mockUrl);
    when(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenReturn(mockPresignedRequest);

    Token token = new Token();
    token.setTokenString("TOKEN999");
    token.setExpiresAt(Instant.now());
    when(tokenService.generateToken(any(File.class), any(Instant.class))).thenReturn(token);

    // Act
    fileService.createUploadUrl(filename, contentType, size, 5, userId);

    // Assert
    ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
    verify(fileRepository, times(1)).save(fileCaptor.capture());

    File savedFile = fileCaptor.getValue();
    assertEquals(filename, savedFile.getFilename());
    assertEquals(contentType, savedFile.getContentType());
    assertEquals(size, savedFile.getSize());
    assertEquals(userId, savedFile.getUserId());
    assertTrue(savedFile.getS3Key().startsWith("uploads/"));
    assertTrue(savedFile.getS3Key().endsWith(filename));
  }

  /** Test case-insensitive extension validation */
  @Test
  @DisplayName("UNIT-FILE-012: Forbidden extensions are case-insensitive")
  public void shouldRejectForbiddenExtensionsIgnoringCase() {
    // Test uppercase extensions
    assertThrows(
        IllegalArgumentException.class,
        () ->
            fileService.createUploadUrl("virus.EXE", "application/octet-stream", 1000, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            fileService.createUploadUrl(
                "script.BAT", "application/octet-stream", 1000, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            fileService.createUploadUrl("script.SH", "application/octet-stream", 1000, null, null));
  }

  /** Test upload with expiration days parameter */
  @Test
  @DisplayName("UNIT-FILE-013: Upload with custom expiration days")
  public void shouldCreateUploadUrlWithCustomExpiration() throws Exception {
    // Arrange
    String filename = "file.xlsx";
    String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    long size = 3_000_000L;
    Integer expirationDays = 30;
    Long userId = 6L;

    when(s3Properties.getBucket()).thenReturn("test-bucket");

    PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
    URL mockUrl =
        new URI("https://s3.amazonaws.com/test-bucket/uploads/test-uuid-file.xlsx").toURL();
    when(mockPresignedRequest.url()).thenReturn(mockUrl);
    when(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
        .thenReturn(mockPresignedRequest);

    Token token = new Token();
    token.setTokenString("TOKEN_CUSTOM_EXP");
    token.setExpiresAt(Instant.now().plus(expirationDays, ChronoUnit.DAYS));
    when(tokenService.generateToken(any(File.class), any(Instant.class))).thenReturn(token);

    // Act
    PresignedUploadResponse response =
        fileService.createUploadUrl(filename, contentType, size, expirationDays, userId);

    // Assert
    assertNotNull(response);
    ArgumentCaptor<Instant> expiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(tokenService, times(1)).generateToken(any(File.class), expiresAtCaptor.capture());

    Instant capturedExpires = expiresAtCaptor.getValue();
    Instant now = Instant.now();
    // Verify that the expiration is approximately 30 days from now (within 1 minute tolerance)
    long secondsDifference = capturedExpires.getEpochSecond() - now.getEpochSecond();
    long expectedSeconds = 30L * 24 * 60 * 60;
    assertTrue(
        secondsDifference >= expectedSeconds - 60 && secondsDifference <= expectedSeconds + 60);
  }

  /** Test deleting a file by its owner deletes the file (and its token via cascade) */
  @Test
  @DisplayName("UNIT-FILE-014: Delete file by owner removes file")
  public void shouldDeleteFileWhenUserIsOwner() throws Exception {
    // Arrange
    String tokenString = "DEL123";
    File file = new File();
    file.setId(20L);
    file.setUserId(42L);
    Token token = new Token();
    token.setTokenString(tokenString);
    token.setFile(file);
    file.setToken(token);

    when(tokenService.validateToken(tokenString)).thenReturn(file);

    User user = new User(42L, "test@mail.com", "password", null);

    // Act
    fileService.deleteMyFile(user, tokenString);

    // Assert: fileRepository.delete called with the file that contained the token
    ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
    verify(fileRepository, times(1)).delete(captor.capture());
    File deleted = captor.getValue();
    assertEquals(file.getId(), deleted.getId());
    assertNotNull(
        deleted.getToken(),
        "Token should be present on the entity passed to delete (cascade will remove it)");
  }

  /** Test deleting a file by a non-owner throws UserNotFileOwnerException */
  @Test
  @DisplayName("UNIT-FILE-015: Delete file by non-owner is forbidden")
  public void shouldThrowWhenUserIsNotOwner() throws Exception {
    // Arrange
    String tokenString = "DEL999";
    File file = new File();
    file.setId(21L);
    file.setUserId(100L); // different owner
    when(tokenService.validateToken(tokenString)).thenReturn(file);

    User user = new User(42L, "test@mail.com", "password", null);

    // Act & Assert
    assertThrows(
        UserNotFileOwnerException.class, () -> fileService.deleteMyFile(user, tokenString));
    verify(fileRepository, never()).delete(any(File.class));
  }

  /** Test deleting a file when token is invalid throws InvalidTokenException */
  @Test
  @DisplayName("UNIT-FILE-016: Delete with invalid token throws InvalidTokenException")
  public void shouldThrowInvalidTokenWhenDeleting() throws Exception {
    // Arrange
    String tokenString = "BADTOKEN";
    when(tokenService.validateToken(tokenString))
        .thenThrow(new InvalidTokenException("Unknown token"));

    User user = new User(42L, "test@mail.com", "password", null);

    // Act & Assert
    assertThrows(InvalidTokenException.class, () -> fileService.deleteMyFile(user, tokenString));
    verify(fileRepository, never()).delete(any(File.class));
  }
}
