package com.datashare.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.datashare.api.dto.LoginRequest;
import com.datashare.api.dto.LoginResponse;
import com.datashare.api.dto.RegisterRequest;
import com.datashare.api.dto.RegisterResponse;
import com.datashare.api.entities.User;
import com.datashare.api.mapper.UserMapper;
import com.datashare.api.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

/** Unit Test Set for AuthController */
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

  @Mock private UserService userService;

  @Mock private UserMapper userMapper;

  @InjectMocks private AuthController authController;

  private RegisterRequest validRegisterRequest;
  private LoginRequest validLoginRequest;
  private User testUser;
  private HttpServletResponse response;
  private RegisterResponse registerResponse;

  @BeforeEach
  void setUp() {
    validRegisterRequest = new RegisterRequest("test@example.com", "ValidPass123!");
    validLoginRequest = new LoginRequest("test@example.com", "ValidPass123!");
    registerResponse = new RegisterResponse("User registered successfully", "1");
    testUser = new User(1L, "test@example.com", "hashedPassword", null);
    response = new MockHttpServletResponse();
  }

  // ════════════════════════════════════════════════════
  // REGISTER TESTS
  // ════════════════════════════════════════════════════

  /** Test that a valid request returns 201 */
  @Test
  @DisplayName("TEST-AUTHCTRL-001: Register Should return 201")
  void testRegisterSuccess() {

    // GIVEN valid request
    when(userService.register(any())).thenReturn(registerResponse);

    // WHEN register
    ResponseEntity<?> result = authController.register(validRegisterRequest);

    // THEN the controller returns 201
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    RegisterResponse body = (RegisterResponse) result.getBody();
    assertThat(body.getMessage()).isEqualTo("User registered successfully");
  }

  @Test
  @DisplayName("TEST-AUTHCTRL-002: Register should handle generic exceptions")
  void testRegisterGenericException() {
    // Arrange
    when(userService.register(any())).thenThrow(new RuntimeException("Database error"));

    // Act & Assert
    try {
      authController.register(validRegisterRequest);
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("Database error");
    }
  }

  // ════════════════════════════════════════════════════
  // LOGIN TESTS
  // ════════════════════════════════════════════════════

  @Test
  @DisplayName("TEST-AUTHCTRL-003: Login should return 200 et create cookie")
  void testLoginSuccess() {
    // Arrange
    String expectedToken = "eyJhbGciOiJIUzI1NiJ9.test.token";
    when(userService.login(any(), any())).thenReturn(expectedToken);

    // Act
    ResponseEntity<?> result = authController.login(validLoginRequest, response);

    // Assert
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    LoginResponse body = (LoginResponse) result.getBody();
    assertThat(body.getMessage()).isEqualTo("Login successful");

    // Vérifier que le cookie a été ajouté
    MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
    assertThat(mockResponse.getHeader("Set-Cookie")).contains("AUTH-TOKEN=" + expectedToken);
  }

  @Test
  @DisplayName("TEST-AUTHCTRL-004: Login should handle generic exceptions")
  void testLoginGenericException() {
    // Arrange
    when(userService.login(any(), any())).thenThrow(new RuntimeException("Server error"));

    // Act & Assert
    try {
      authController.login(validLoginRequest, response);
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("Server error");
    }
  }

  @Test
  @DisplayName("TEST-AUTHCTRL-005: Cookie devrait avoir les bons attributs")
  void testCookieAttributes() {
    // Arrange
    String expectedToken = "eyJhbGciOiJIUzI1NiJ9.test.token";
    when(userService.login(anyString(), anyString())).thenReturn(expectedToken);

    // Act
    authController.login(validLoginRequest, response);

    // Assert
    MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
    String setCookie = mockResponse.getHeader("Set-Cookie");

    assertThat(setCookie).contains("AUTH-TOKEN=" + expectedToken);
    assertThat(setCookie).contains("HttpOnly");
    assertThat(setCookie).contains("Secure");
    assertThat(setCookie).containsIgnoringCase("SameSite=Strict");
    assertThat(setCookie).contains("Path=/");
    assertThat(setCookie).contains("Max-Age=604800"); // 7 days
  }

  @Test
  @DisplayName("TEST-AUTHCTRL-006: getCurrentUser shourl return user information")
  void testGetCurrentUser() {
    // Act
    ResponseEntity<?> result = authController.getCurrentUser(testUser);

    // Assert
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isInstanceOf(Map.class);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) result.getBody();
    assertThat(body).containsKey("email");
    assertThat(body).containsKey("authorities");
    assertThat(body.get("email")).isEqualTo(testUser.getEmail());
  }
}
