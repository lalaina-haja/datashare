package com.datashare.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.datashare.api.dto.LoginRequest;
import com.datashare.api.dto.RegisterRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

/** Integration Test Set for CSRF Protection */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CSRFProtectionIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private static final String TEST_EMAIL = "csrf.test@example.com";
  private static final String TEST_PASSWORD = "SecurePass123!";

  @Value("${web-url}")
  private String webUrl;

  /** Create test user */
  @BeforeEach
  void setUp() throws Exception {
    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                objectMapper.writeValueAsString(new RegisterRequest(TEST_EMAIL, TEST_PASSWORD))));
  }

  /** Test that login doesn't require CSRF token */
  @Test
  @DisplayName("INTEG-CSRF-001: Login should not require CSRF token")
  void testLoginNoCSRFRequired() throws Exception {

    // WHEN login without CSRF token THEN returns OK
    mockMvc
        .perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
        .andExpect(status().isOk());
  }

  /** Test that register doesn't require CSRF token */
  @Test
  @DisplayName("INTEG-CSRF-002: Register should not require CSRF token")
  void testRegisterNoCSRFRequired() throws Exception {

    // WHEN register without CSRF token THEN returns OK
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new RegisterRequest("new.user@test.com", "ValidPass123!"))))
        .andExpect(status().isCreated());
  }

  /** Test that an authenticated endpoint requires CSRF token */
  @Test
  @DisplayName("INTEG-CSRF-003: Protected Endpoints should require a CSRF token")
  void testProtectedEndpointsRequireCSRF() throws Exception {

    // GIVEN the token
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
            .andReturn();

    Cookie authCookie = loginResult.getResponse().getCookie("AUTH-TOKEN");
    assertThat(authCookie).isNotNull();

    // WHEN GET /files/1 without CSRF token THEN returns forbidden
    mockMvc.perform(delete("/files/1").cookie(authCookie)).andExpect(status().isForbidden());
  }

  /** Test that a request with CSRF token is accepted */
  @Test
  @DisplayName("INTEG-CSRF-004: Should accept requests with valid CSRF token")
  void testValidCSRFToken() throws Exception {

    // GIVEN Login
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
            .andReturn();
    Cookie authCookie = loginResult.getResponse().getCookie("AUTH-TOKEN");

    // WHEN GET protected resource with valid CSRF token THEN CSRF passes
    mockMvc
        .perform(delete("/files/1").with(csrf()).cookie(authCookie))
        .andExpect(status().isNotFound()); // File doesn't exist, but CSRF passed
  }

  /** Test that a request with invalid CSRF token is rejected */
  @Test
  @DisplayName("INTEG-CSRF-005: Should reject requests with invalid CSRF token")
  void testInvalidCSRFToken() throws Exception {

    // GIVEN login
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
            .andReturn();
    Cookie authCookie = loginResult.getResponse().getCookie("AUTH-TOKEN");

    // WHEN GET protected resource with an invalid CSRF token THEN returns forbidden
    mockMvc
        .perform(delete("/files/1").cookie(authCookie).header("X-XSRF-TOKEN", "invalid-csrf-token"))
        .andExpect(status().isForbidden());
  }

  /** Test that a OPTIONS request does not require CSRF */
  @Test
  @DisplayName("INTEG-CSRF-006: OPTIONS request (CORS preflight) does not require CSRF")
  void testOptionsRequestNoCSRF() throws Exception {
    mockMvc
        .perform(
            options("/files")
                .header("Origin", webUrl)
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk());
  }

  /** Test that logout accepts requests with valid CSRF */
  @Test
  @DisplayName("TEST-CSRF-007: Logout should accept requests with valid CSRF")
  void testLogoutWithCSRF() throws Exception {

    // GIVEN Login
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
            .andReturn();
    Cookie authCookie = loginResult.getResponse().getCookie("AUTH-TOKEN");

    // WHEN Logout with CSRF THEN returns OK
    mockMvc
        .perform(post("/auth/logout").with(csrf()).cookie(authCookie))
        .andExpect(status().isOk());
  }
}
