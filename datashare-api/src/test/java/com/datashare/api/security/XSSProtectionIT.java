package com.datashare.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.datashare.api.dto.LoginRequest;
import com.datashare.api.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

/** Test set for XSS protection */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class XSSProtectionIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  /** Test that emails with malicious script */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "<script>alert('XSS')</script>@test.com",
        "test@test.com<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>@test.com",
        "javascript:alert('XSS')@test.com",
        "<svg/onload=alert('XSS')>@test.com",
        "test@test.com'; DROP TABLE users; --",
        "<iframe src='javascript:alert(\"XSS\")'></iframe>@test.com",
        "test@test.com<body onload=alert('XSS')>",
        "<<SCRIPT>alert('XSS');//<</SCRIPT>@test.com",
        "test@<script>document.cookie</script>.com"
      })
  @DisplayName("INTEG-XSS-001: Should reject email with malicious script")
  void testXSSInEmail(String maliciousEmail) throws Exception {

    // GIVEN thet request payload
    RegisterRequest request = new RegisterRequest(maliciousEmail, "ValidPass123!");

    // WHEN POST /auth/register with malicious email THEN returns bad request
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
  }

  /** Test that the cookie httponly is invisible to JavaScript */
  @Test
  @DisplayName("INTEG-XSS-002: Cookie HttpOnly should be invisible to JavaScript")
  void testCookieNotAccessibleToJavaScript() throws Exception {

    // GIVEN the request payload
    RegisterRequest registerRequest = new RegisterRequest("testxss@example.com", "ValidPass123!");

    // WHEN POST /auth/register
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    // AND POST /auth/login
    var loginResult =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andReturn();

    // THEN the cookie is Http only
    var cookie = loginResult.getResponse().getCookie("AUTH-TOKEN");
    assertThat(cookie.isHttpOnly()).isTrue();
  }

  /** Test that the response does not contain unescapted HTML */
  @Test
  @DisplayName("INTEG-XSS-003: API Response should not contain unescapted HTML")
  void testHTMLEscapingInResponses() throws Exception {

    // GIVEN email with HTML
    String emailWithHTML = "test<b>bold</b>@test.com";

    // WHEN POST /auth/register
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        jsonMapper.writeValueAsString(
                            new RegisterRequest(emailWithHTML, "ValidPass123!"))))
            .andExpect(status().isBadRequest())
            .andReturn();

    // THEN response does not contain unescapted HTML
    String response = result.getResponse().getContentAsString();
    assertThat(response).doesNotContain("<b>");
    assertThat(response).doesNotContain("</b>");
  }

  /** Test that security headers are present */
  @Test
  @DisplayName("INTEG-XSS-004: Security Headers should be present")
  void testSecurityHeaders() throws Exception {

    // WHEN POST /auth/login
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        jsonMapper.writeValueAsString(
                            new LoginRequest("test@test.com", "ValidPass123!"))))
            .andReturn();

    // THEN security headers are present
    assertThat(result.getResponse().getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
    assertThat(result.getResponse().getHeader("X-Frame-Options")).isIn("DENY", "SAMEORIGIN");
    assertThat(result.getResponse().getHeader("X-XSS-Protection"))
        .isIn("1; mode=block", "0"); // 0 for modern browsers
  }

  /** Test that Content-Type is always application/json */
  @Test
  @DisplayName("INTEG-XSS-005: Content-Type should be application/json")
  void testContentTypeSecurity() throws Exception {

    // WHEN POST /auth/login
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        jsonMapper.writeValueAsString(
                            new LoginRequest("test@test.com", "ValidPass123!"))))
            .andReturn();

    // THEN the response content-type is application/json
    String contentType = result.getResponse().getContentType();
    assertThat(contentType).contains("application/json");
    assertThat(contentType).doesNotContain("text/html");
  }

  /** Test that encoded XSS payloads are rejected */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "test%3Cscript%3Ealert%28%27XSS%27%29%3C%2Fscript%3E@test.com", // <script>alert('XSS')</script>
        "test%3Cimg%20src%3Dx%20onerror%3Dalert%28%27XSS%27%29%3E@test.com", // <img src=x
        // onerror=alert('XSS')>
        "test%3Ciframe%20src%3D%22evil.com%22%3E@test.com", // <iframe src="evil.com">
        "test%22%3E%3Cscript%3Ealert%28%27XSS%27%29%3C%2Fscript%3E@test.com", // "><script>alert('XSS')</script>
        "test%3Cbody%20onload%3Dalert%28%27XSS%27%29%3E@test.com" // <body onload=alert('XSS')>
      })
  @DisplayName("INTEG-XSS-006: Encoded XSS payloads should be rejected")
  void testEncodedXSSPayloads(String encodedXSS) throws Exception {

    // GIVEN an URL encoded XSS
    RegisterRequest request = new RegisterRequest(encodedXSS, "ValidPass123!");

    // WHEN POST /auth/register THEN rejected
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.email").value("Invalid email format"));
  }

  /** Test that valid email are not rejected */
  @Test
  @DisplayName("INTEG-XSS-007: Valid Emails should not be rejected")
  void testValidEmailsNotRejected() throws Exception {
    String[] validEmails = {
      "user@example.com",
      "john.doe@company.co.uk",
      "test+tag@domain.com",
      "user_name@sub.domain.com"
    };

    for (String validEmail : validEmails) {
      RegisterRequest request = new RegisterRequest(validEmail, "ValidPass123!");

      mockMvc
          .perform(
              post("/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(jsonMapper.writeValueAsString(request)))
          .andDo(
              mvcResult -> {
                System.out.println("Valid email accepted: " + validEmail);
              })
          .andExpect(status().isCreated());
    }
  }
}
