package com.datashare.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.datashare.api.dto.LoginRequest;
import com.datashare.api.entities.User;
import jakarta.servlet.http.Cookie;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

/** Integration Test Set for Cookie and token */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CookieSecurityIT {

  /** Test email address used for registration */
  private static final String EMAIL = "test@example.com";

  /** Test password used for registration */
  private static final String PASSWORD = "Secur3#2024";

  /** MockMvc instance for testing HTTP requests */
  @Autowired private MockMvc mockMvc;

  /** ObjectMapper for JSON serialization/deserialization */
  @Autowired private ObjectMapper objectMapper;

  /** Create existing user */
  @BeforeEach
  void setup() throws Exception {
    mockMvc.perform(
        post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new User(null, EMAIL, PASSWORD, null))));
  }

  /** Test that successful login returns a HttpOnly cookie */
  @Test
  @DisplayName("INTEG-COOKIE-001: Successful login should create HttpOnly cookie")
  void testHttpOnlyCookieCreation() throws Exception {

    // GIVEN the correct credentials
    LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);

    // WHEN POST /auth/login
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    // THEN returns cookie HttpOnly
    Collection<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
    String authTokenHeader = extractSetCookieHeader(setCookieHeaders, "AUTH-TOKEN");
    assertThat(authTokenHeader).as("AUTH-TOKEN cookie should be present").isNotNull();
    assertThat(authTokenHeader.contains("HttpOnly")); // HttpOnly
    assertThat(authTokenHeader.contains("Secure")); // Secure
    assertThat(authTokenHeader.contains("Path=/"));
    assertThat(authTokenHeader.contains("Max-Age=604800"));
  }

  /** Test that SameSite=Strict in the Set-Cookie header */
  @Test
  @DisplayName("INTEG-COOKIE-002: Should have SameSite=Strict in Set-Cookie header")
  void testSameSiteStrict() throws Exception {

    // GIVEN the correct credentials
    LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);

    // WHEN POST /auth/login
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    // THEN AUTH-TOKEN Set-Cookie header has SameSite=Strict
    Collection<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
    String authTokenHeader = extractSetCookieHeader(setCookieHeaders, "AUTH-TOKEN");
    assertThat(authTokenHeader).isNotNull();
    assertThat(authTokenHeader).contains("AUTH-TOKEN=");
    assertThat(authTokenHeader).contains("HttpOnly");
    assertThat(authTokenHeader).contains("Secure");
    assertThat(authTokenHeader).contains("SameSite=Strict");
    assertThat(authTokenHeader).contains("Path=/");
    assertThat(authTokenHeader).contains("Max-Age=");
  }

  /** Test that authenticated path accepts valid cookie */
  @Test
  @DisplayName("INTEG-COOKIE-003: Should accept requests with valid cookie")
  void testAuthenticatedRequestWithCookie() throws Exception {

    // GIVEN the correct credentials
    LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);

    // WHEN Login to get cookie
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
    Cookie authCookie = loginResult.getResponse().getCookie("AUTH-TOKEN");

    // THEN Request with cookie is OK
    mockMvc
        .perform(get("/auth/me").cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(EMAIL));
  }

  /** Test that a request without cookie is rejected */
  @Test
  @DisplayName("TEST-COOKIE-004: Should reject request without cookie")
  void testUnauthenticatedRequest() throws Exception {
    mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
  }

  /** Test that a request with invalid cookie is rejected */
  @Test
  @DisplayName("TEST-COIKIE-005: Should reject request with invalid cookie")
  void testInvalidCookie() throws Exception {
    Cookie invalidCookie = new Cookie("AUTH-TOKEN", "invalid.jwt.token");

    mockMvc.perform(get("/auth/me").cookie(invalidCookie)).andExpect(status().isUnauthorized());
  }

  /** Helper method to extract the complete header Set-Cookie */
  private String extractSetCookieHeader(Collection<String> setCookieHeaders, String cookieName) {
    return setCookieHeaders.stream()
        .filter(header -> header.startsWith(cookieName + "="))
        .findFirst()
        .orElse(null);
  }
}
