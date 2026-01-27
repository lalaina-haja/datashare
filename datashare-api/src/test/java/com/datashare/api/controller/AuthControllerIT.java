package com.datashare.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.datashare.api.dto.RegisterRequest;
import com.datashare.api.entities.User;
import com.datashare.api.repository.UserRepository;
import com.datashare.api.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

/**
 * Integration tests for the authentication controller.
 *
 * <p>This test class validates the authentication endpoints including user registration and login.
 * It uses Testcontainers with PostgreSQL to provide a real database environment for testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerIT {

  /** API path for user registration */
  private static final String PATH_REGISTER = "/auth/register";

  /** API path for user login */
  private static final String PATH_LOGIN = "/auth/login";

  /** Test email address used for registration */
  private static final String EMAIL = "name@domain.com";

  /** Test password used for registration */
  private static final String PASSWORD = "ValidPass123!";

  /** MockMvc instance for testing HTTP requests */
  @Autowired private MockMvc mockMvc;

  /** UserRepository for database operations */
  @Autowired private UserRepository userRepository;

  /** UserService for user management */
  @Autowired private UserService userService;

  /** ObjectMapper for JSON serialization/deserialization */
  @Autowired private ObjectMapper objectMapper;

  /**
   * Cleans up test data after each test. Deletes all users from the repository to ensure test
   * isolation.
   */
  @AfterEach
  public void tearDown() {
    userRepository.deleteAll();
  }

  /**
   * Tests successful user registration via POST /auth/register.
   *
   * <p>This test verifies that:
   *
   * <ul>
   *   <li>A registration request with valid credentials returns HTTP 201 (Created)
   *   <li>The response contains a success message
   *   <li>The response includes a numeric userId
   *   <li>The user is persisted in the database
   * </ul>
   *
   * @throws Exception if the HTTP request fails
   */
  @Test
  @DisplayName("Register successful saves user in repository")
  public void register_user_successful() throws Exception {

    // GIVEN the correct credentials
    RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

    // WHEN POST /auth/register THEN returns Created
    mockMvc
        .perform(
            post(PATH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("User registered successfully"))
        .andExpect(jsonPath("$.email").isNotEmpty());

    // AND the user is created in database
    assertTrue(userRepository.findByEmail(EMAIL).isPresent(), "User should be saved in database");
  }

  /**
   * Tests that registration fails with missing required fields.
   *
   * <p>This parameterized test verifies that when a required field is missing, the registration
   * endpoint returns HTTP 400 (Bad Request) with an appropriate error message indicating which
   * field is missing.
   *
   * @param missing the name of the field that is missing in the request
   * @throws Exception if the HTTP request fails
   */
  @ParameterizedTest(name = "register_with_missing_{0}_field_returns_bad_request")
  @ValueSource(strings = {"email", "password"})
  @DisplayName("Register with missing field returns bad request")
  public void register_with_missing_field_returns_bad_request(String missing) throws Exception {

    // GIVEN a field is missing
    RegisterRequest request =
        new RegisterRequest(
            missing.equals("email") ? null : EMAIL, missing.equals("password") ? null : PASSWORD);

    // WHEN POST /register THEN returns bad request with correct error message
    mockMvc
        .perform(
            post(PATH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.errors." + missing)
                .value((missing.equals("email") ? "Email" : "Password") + " is required"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.path").value(PATH_REGISTER))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.timestamp").isNotEmpty());
  }

  /** Test that registering with invalid email and password returns bad request */
  @Test
  @DisplayName("Register with invalid credentials returns bad request")
  public void register_with_invalid_credentials_returns_bad_request() throws Exception {

    // GIVEN an invalid email and password
    RegisterRequest request = new RegisterRequest("invalid-email", "short");

    // WHEN POST /register THEN returns bad request with correct error message
    mockMvc
        .perform(
            post(PATH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.email").value("Invalid email format"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.path").value(PATH_REGISTER))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.timestamp").isNotEmpty());
  }

  /** Test that registering existing user returns bad request */
  @Test
  @DisplayName("Register existing user returns bad request")
  public void register_existing_user_returns_bad_request() throws Exception {

    // GIVEN existing user
    User existingUser = new User(null, EMAIL, PASSWORD, null);
    userRepository.save(existingUser);

    // WHEN POST /register THEN returns bad request with correct error message
    RegisterRequest request = new RegisterRequest(EMAIL, "Str0ng@Pass");
    mockMvc
        .perform(
            post(PATH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email is already in use: " + EMAIL))
        .andExpect(jsonPath("$.path").value(PATH_REGISTER))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.timestamp").isNotEmpty());
  }

  /** Test that login success returns cookie with token */
  @Test
  @DisplayName("Login successful returns cookie with token")
  public void login_success_returns_token() throws Exception {

    // GIVEN existing user and correct credentials
    User existingUser = new User(null, EMAIL, PASSWORD, null);
    userService.register(existingUser);
    RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

    // WHEN POST /login THEN returns token and expiration information
    MvcResult result =
        mockMvc
            .perform(
                post(PATH_LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    Cookie cookie = result.getResponse().getCookie("AUTH-TOKEN");
    assertThat(cookie).isNotNull(); // Token
  }

  /** Test that Login with missing field returns bad request */
  @ParameterizedTest(name = "login_with_missing_{0}_field_returns_bad_request")
  @ValueSource(strings = {"email", "password"})
  public void login_with_missing_field_returns_bad_request(String missing) throws Exception {

    // GIVEN a field is missing
    RegisterRequest request =
        new RegisterRequest(
            missing.equals("email") ? null : EMAIL, missing.equals("password") ? null : PASSWORD);

    // WHEN POST /register THEN returns bad request with correct error message
    mockMvc
        .perform(
            post(PATH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.errors." + missing)
                .value((missing.equals("email") ? "Email" : "Password") + " is required"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.path").value(PATH_LOGIN))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.timestamp").isNotEmpty());
  }

  /** Test that login with inexisting user returns invalid credentials */
  @Test
  @DisplayName("Login with inexisting user returns invalid credentials")
  public void login_with_inexisting_user_returns_invalid_credentials() throws Exception {

    // GIVEN inexisting user
    RegisterRequest request = new RegisterRequest("inexisting-user", "password");

    // WHEN POST /register THEN returns bad request with correct error message
    mockMvc
        .perform(
            post(PATH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid email"))
        .andExpect(jsonPath("$.path").value(PATH_LOGIN))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.timestamp").isNotEmpty());
  }

  /** Test that login with wrong password returns invalid credentials */
  @Test
  @DisplayName("Login with wrong password returns invalid credentials")
  public void login_with_wrong_password_returns_invalid_credentials() throws Exception {

    // GIVEN the existing user and the request with wrong password
    User existingUser = new User(null, EMAIL, PASSWORD, null);
    userRepository.save(existingUser);
    RegisterRequest request = new RegisterRequest(EMAIL, "wrong-password");

    // WHEN POST /register THEN returns bad request with correct error message
    mockMvc
        .perform(
            post(PATH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid password"))
        .andExpect(jsonPath("$.path").value(PATH_LOGIN))
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.timestamp").isNotEmpty());
  }
}
