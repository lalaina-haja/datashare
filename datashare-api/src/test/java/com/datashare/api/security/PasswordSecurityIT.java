package com.datashare.api.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.datashare.api.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Test Set for Password Security */
@SpringBootTest
@AutoConfigureMockMvc
public class PasswordSecurityIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  /** Test that weak passwords are rejected */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "12345", // Too short
        "1234567", // Too short
        "password", // No digit, uppercase, special character
        "PASSWORD123", // No lowercase, special character
        "Password", // No digit, special character
        "Pass123", // Too short
        "aaaaaaaa", // No digit, uppercase, special character
        "AAAAAAAA", // No digit, lowercase, special character
        "12345678", // No character, special character
        "abcdefgh", // No digit, uppercase, special character
        "Abcdefgh", // No digit, special character
        "Password123" // No special character
      })
  @DisplayName("INTEG-PSWD-001: Should reject weak passwords")
  void testWeakPasswordRejection(String weakPassword) throws Exception {

    // GIVEN the weak password
    RegisterRequest request = new RegisterRequest("pwd.test@test.com", weakPassword);

    // WHEN POST /auth/register THEN BadRequest returned
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").exists());
  }

  /** Test that strong passwords are accepted */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "ValidPass123!",
        "Str0ng@Pass",
        "Secur3#2024",
        "MyP@ssw0rd",
        "C0mpl3x!Pass",
        "Test@123Pass",
        "Azerty123!@#",
        "Qwerty@12345"
      })
  @DisplayName("INTEG-PSWD-002: Should accept strong passwords")
  void testStrongPasswordAcceptance(String strongPassword) throws Exception {

    // GIVEN the strong password
    RegisterRequest request =
        new RegisterRequest(
            "strong.pwd." + System.currentTimeMillis() + "@test.com", strongPassword);

    // WHEN POST /auth/register THEN OK
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  /** Test that too short passwords are rejected */
  @Test
  @DisplayName("INTEG-PSWD-003: Password should contain at least 8 characters")
  void testMinimumLength() throws Exception {

    // GIVEN the password less than 8 characters
    RegisterRequest request = new RegisterRequest("test@test.com", "Pass1!");

    // WHEN POST /auth/register THEN BadRequest returned
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").value(containsString("100")));
  }

  /** Test that a password that does not contain at least an uppercase is rejected */
  @Test
  @DisplayName("INTEG-PSWD-004: Password should contains at least an uppercase")
  void testUppercaseRequired() throws Exception {

    // GIVEN the password without uppercase
    RegisterRequest request = new RegisterRequest("test@test.com", "password123!");

    // WHEN POST /auth/register THEN BadRequest returned
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").value(containsString("uppercase")));
  }

  /** Test that a password that does not contain at least a lowercase is rejected */
  @Test
  @DisplayName("INTEG-PSWD-005: Password should contain at least a lowercase")
  void testLowercaseRequired() throws Exception {

    // GIVEN the password without lowercase
    RegisterRequest request = new RegisterRequest("test@test.com", "PASSWORD123!");

    // WHEN POST /auth/register THEN BadRequest returned
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").value(containsString("lowercase")));
  }

  /** Test that a password that does not contain at least a digit is rejected */
  @Test
  @DisplayName("INTEG-PSWD-006: Password should contain at leat a digit")
  void testDigitRequired() throws Exception {

    // GIVEN the password without digit
    RegisterRequest request = new RegisterRequest("test@test.com", "Password!");

    // WHEN POST /auth/register THEN BadRequest returned
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").value(containsString("digit")));
  }

  /** Test that a password that does not contain at least a special character is rejected */
  @Test
  @DisplayName("INTEG-PSWD-007: Password should contain at least a special character")
  void testSpecialCharacterRequired() throws Exception {

    // GIVEN the password without special character
    RegisterRequest request = new RegisterRequest("test@test.com", "Password123");

    // WHEN POST /auth/register THEN BadRequest returned
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").value(containsString("special")));
  }

  /** Test that a password that exceeds 100 characters is rejected */
  @Test
  @DisplayName("INTEG-PSWD-008: Password should not exceed 100 characters")
  void testMaximumLength() throws Exception {

    // GIVEN the too long password
    String tooLongPassword = "Aa1!".repeat(30); // 120 characters
    RegisterRequest request = new RegisterRequest("test@test.com", tooLongPassword);

    // WHEN POST /auth/register THEN BadRequest returned
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").value(containsString("100")));
  }

  /** Test that a password that contains spaces is accepted */
  @Test
  @DisplayName("INTEG-PSWD-009: Spaces at start and end of the password are accepted")
  void testPasswordTrimming() throws Exception {

    // GIVEN the password with spaces at the start and the end
    RegisterRequest request = new RegisterRequest("trim.test@test.com", "  ValidPass123!  ");

    // WHEN POST /auth/register THEN OK
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }
}
