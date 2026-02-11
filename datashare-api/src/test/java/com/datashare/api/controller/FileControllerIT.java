package com.datashare.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.datashare.api.dto.LoginRequest;
import com.datashare.api.entities.File;
import com.datashare.api.entities.Token;
import com.datashare.api.entities.User;
import com.datashare.api.repository.FileRepository;
import com.datashare.api.repository.TokenRepository;
import com.datashare.api.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileControllerIT {

  /** Test email address used for registration */
  private static final String EMAIL = "test@example.com";

  /** Test password used for registration */
  private static final String PASSWORD = "Secur3#2024";

  /** ObjectMapper for JSON serialization/deserialization */
  @Autowired private JsonMapper jsonMapper;

  @Autowired private MockMvc mockMvc;

  @Autowired private FileRepository fileRepository;

  @Autowired private TokenRepository tokenRepository;

  @Autowired private UserRepository userRepository;

  private Cookie authCookie;

  private Long userId;

  @BeforeEach
  public void setup() throws Exception {

    // Reset database
    // fileRepository.deleteAll();
    // tokenRepository.deleteAll();
    userRepository.deleteAll();

    // Register user and set userID
    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(new User(null, EMAIL, PASSWORD, null))));
    userId = userRepository.findByEmail(EMAIL).get().getId();

    // Login user and set authCookie
    authCookie =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(new LoginRequest(EMAIL, PASSWORD))))
            .andReturn()
            .getResponse()
            .getCookie("AUTH-TOKEN");
  }

  /** Test that a presigned upload URL is successfully generated and metadata is stored */
  @Test
  @DisplayName("INTEG-FILE-001: Get presigned URL successful")
  public void shouldReturnPresignedUrl() throws Exception {

    // WHEN POST /files/upload
    mockMvc
        .perform(
            post("/files/upload")
                .with(csrf())
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {"filename":"test.png","contentType":"image/png", "size": 12, "expirationDays": 3}
            """))

        // THEN the response is OK
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uploadUrl").exists())
        .andExpect(jsonPath("$.tokenString").exists())
        .andExpect(jsonPath("$.expiresAt").exists());

    // AND the file metadata are saved in database
    List<File> files = fileRepository.findByUserId(userId);
    assertEquals(1, files.size());
    assertEquals("test.png", files.get(0).getFilename());
  }

  /** Test that forbidden extensions are rejected */
  @Test
  @DisplayName("INTEG-FILE-002: Forbidden extensions are rejected")
  public void shouldRejectForbiddenExtensions() throws Exception {

    // WHEN POST /files/upload with forbidden file extension
    mockMvc
        .perform(
            post("/files/upload")
                .with(csrf())
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                { "filename": "virus.exe", "contentType": "application/octet-stream", "size": 1000 }
                """))

        // THEN returns Bad Request
        .andExpect(status().isBadRequest());
  }

  /** Test that file too large is rejected */
  @Test
  @DisplayName("INTEG-FILE-003: File too large is rejected")
  void shouldRejectTooLargeFiles() throws Exception {

    // WHEN POST /files/upload with file too large
    mockMvc
        .perform(
            post("/files/upload")
                .with(csrf())
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                { "filename": "big.bin", "contentType": "application/octet-stream", "size": 2000000000 }
            """))

        // THEN returns Bad Request
        .andExpect(status().isBadRequest());
  }

  /** Test that the user can get his file history */
  @Test
  @DisplayName("INTEG-FILE-004: Get user's file history")
  public void shouldReturnUserHistory() throws Exception {

    // GIVEN the user's file
    File f = new File();
    f.setUserId(userId);
    f.setFilename("test.png");
    f.setContentType("image/png");
    f.setSize(1000L);
    f.setS3Key("uploads/x");
    f.setCreatedAt(Instant.now());
    fileRepository.save(f);

    Token t = new Token();
    t.setTokenString("token");
    t.setExpiresAt(Instant.now());
    t.setFile(f);
    f.setToken(t);
    tokenRepository.save(t);

    // WHEN GET /files/my
    mockMvc
        .perform(get("/files/my").with(csrf()).cookie(authCookie))

        // THEN got a list containing the file
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].filename").value("test.png"));
  }

  /** Test that a presigned download URL is successfully retrieved */
  @Test
  @DisplayName("INTEG-FILE-005: Get presigned download URL with valid token")
  public void shouldReturnPresignedDownloadUrl() throws Exception {

    // GIVEN a file with a valid token
    File file = new File();
    file.setUserId(userId);
    file.setFilename("document.pdf");
    file.setContentType("application/pdf");
    file.setSize(50000L);
    file.setS3Key("uploads/uuid-document.pdf");
    file.setCreatedAt(Instant.now());
    fileRepository.save(file);

    Token token = new Token();
    token.setTokenString("ABCDEF");
    token.setExpiresAt(Instant.now().plus(Duration.ofHours(24)));
    token.setFile(file);
    file.setToken(token);
    tokenRepository.save(token);

    // WHEN GET /files/download/{tokenString}
    mockMvc
        .perform(get("/files/download/ABCDEF"))

        // THEN the response is OK
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.filename").value("document.pdf"))
        .andExpect(jsonPath("$.contentType").value("application/pdf"))
        .andExpect(jsonPath("$.size").value(50000L))
        .andExpect(jsonPath("$.downloadUrl").exists())
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.expiresAt").exists());
  }

  /** Test that invalid token returns unauthorized */
  @Test
  @DisplayName("INTEG-FILE-006: Get presigned download URL with invalid token")
  public void shouldReturn401ForInvalidToken() throws Exception {

    // WHEN GET /files/download with invalid token string
    mockMvc
        .perform(get("/files/download/INVALID"))

        // THEN returns Unauthorized
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Unknown token"));
  }

  /** Test that expired token returns unautorized */
  @Test
  @DisplayName("INTEG-FILE-007: Get presigned download URL with expired token")
  public void shouldReturn401ForExpiredToken() throws Exception {

    // GIVEN a file with an expired token
    File file = new File();
    file.setUserId(userId);
    file.setFilename("old_file.txt");
    file.setContentType("text/plain");
    file.setSize(1000L);
    file.setS3Key("uploads/uuid-old.txt");
    file.setCreatedAt(Instant.now());
    fileRepository.save(file);

    Token token = new Token();
    token.setTokenString("EXPIRED");
    // Set expiration to 1 hour ago
    token.setExpiresAt(Instant.now().minus(Duration.ofHours(1)));
    token.setFile(file);
    file.setToken(token);
    tokenRepository.save(token);

    // WHEN GET /files/download with expired token
    mockMvc
        .perform(get("/files/download/EXPIRED"))

        // THEN returns Bad Request
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Expired token"));
  }

  /** Test that user can get multiple files */
  @Test
  @DisplayName("INTEG-FILE-008: Get user's file history with multiple files")
  public void shouldReturnMultipleFilesForUser() throws Exception {

    // GIVEN multiple files for the user
    for (int i = 1; i <= 3; i++) {
      File f = new File();
      f.setUserId(userId);
      f.setFilename("file" + i + ".txt");
      f.setContentType("text/plain");
      f.setSize(1000L * i);
      f.setS3Key("uploads/file" + i);
      f.setCreatedAt(Instant.now());
      fileRepository.save(f);

      Token t = new Token();
      t.setTokenString("TOKEN" + i);
      t.setExpiresAt(Instant.now().plus(Duration.ofDays(7)));
      t.setFile(f);
      f.setToken(t);
      tokenRepository.save(t);
    }

    // WHEN GET /files/my
    mockMvc
        .perform(get("/files/my").with(csrf()).cookie(authCookie))

        // THEN got a list with all files
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].filename").value("file1.txt"))
        .andExpect(jsonPath("$[1].filename").value("file2.txt"))
        .andExpect(jsonPath("$[2].filename").value("file3.txt"));
  }

  /** Test that user gets empty list when they have no files */
  @Test
  @DisplayName("INTEG-FILE-009: Get user's file history returns empty list when no files")
  public void shouldReturnEmptyListForUserWithNoFiles() throws Exception {

    // WHEN GET /files/my with no files uploaded
    mockMvc
        .perform(get("/files/my").with(csrf()).cookie(authCookie))

        // THEN got an empty list
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  /** Test that users only see their own files */
  @Test
  @DisplayName("INTEG-FILE-010: User only sees their own files, not other users' files")
  public void shouldReturnOnlyCurrentUserFiles() throws Exception {

    // GIVEN another user with files
    User otherUser = new User(23L, "other@example.com", "Password#2024", null);

    // AND the other user has a file
    File otherUserFile = new File();
    otherUserFile.setUserId(otherUser.getId());
    otherUserFile.setFilename("other_file.txt");
    otherUserFile.setContentType("text/plain");
    otherUserFile.setSize(5000L);
    otherUserFile.setS3Key("uploads/other");
    otherUserFile.setCreatedAt(Instant.now());
    fileRepository.save(otherUserFile);

    Token otherToken = new Token();
    otherToken.setExpiresAt(Instant.now());
    otherToken.setTokenString("otherToken");
    otherToken.setFile(otherUserFile);
    otherUserFile.setToken(otherToken);
    tokenRepository.save(otherToken);

    // AND the current user has a file
    File currentUserFile = new File();
    currentUserFile.setUserId(userId);
    currentUserFile.setFilename("my_file.txt");
    currentUserFile.setContentType("text/plain");
    currentUserFile.setSize(3000L);
    currentUserFile.setS3Key("uploads/mine");
    currentUserFile.setCreatedAt(Instant.now());
    fileRepository.save(currentUserFile);

    Token token = new Token();
    token.setExpiresAt(Instant.now());
    token.setTokenString("currentToken");
    token.setFile(currentUserFile);
    currentUserFile.setToken(token);
    tokenRepository.save(token);

    // WHEN GET /files/my
    mockMvc
        .perform(get("/files/my").with(csrf()).cookie(authCookie))

        // THEN only the current user's file is returned
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].filename").value("my_file.txt"));
  }
}
