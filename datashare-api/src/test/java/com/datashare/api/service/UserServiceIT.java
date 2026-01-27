package com.datashare.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.datashare.api.entities.User;
import com.datashare.api.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Integration Test Set for UserService */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceIT {

  private User user;

  private static final String EMAIL = "name@example.com";
  private static final String PASSWORD = "PASSWORD";

  @Autowired private UserService userService;

  @Autowired private UserRepository userRepository;

  /** Reinitialise user repository */
  @BeforeEach
  public void setUp() {
    userRepository.deleteAll();
    user = new User(null, EMAIL, PASSWORD, null);
    userService.register(user);
  }

  /** Test that register an already registered user throws IllegalArgumentException */
  @Test
  @DisplayName("INTEG-REGISTER-001: Register existing user throws IllegalArgumentException")
  public void register_existing_user_throw_IllegalArgumentException() throws Exception {

    // WHEN register with existing user
    Exception exception =
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(user));

    // THEN expect exception thrown with message "Email is already in use: ..."
    assertThat(exception.getMessage()).isEqualTo("Email is already in use: " + user.getEmail());
  }

  /** Test that when a registered user logs in then a token is returned */
  @Test
  @DisplayName("INTEG-LOGIN-001: Login successful returns token")
  public void login_successful_returns_token() throws Exception {

    // WHEN login with existing user
    String token = userService.login(EMAIL, PASSWORD);

    // THEN a token is generated
    assertThat(token).isNotBlank();
  }

  /** Test that a login with inexisting user throws IllegalArgumentException */
  @Test
  @DisplayName("INTEG-LOGIN-002: Login with inexisting user throws IllegalArgumentException")
  public void login_with_inexisting_user_throws_IllegalArgumentException() throws Exception {

    // WHEN login THEN exception thrown
    Exception exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> userService.login("unknown", PASSWORD));

    // AND message is "Invalid email"
    assertThat(exception.getMessage()).isEqualTo("Invalid email");
  }

  /** Test that login with wrong password throws IllegalArgumentException */
  @Test
  @DisplayName("INTEG-LOGIN-003: Login with wrong password throws IllegalArgumentException")
  public void login_with_wrong_password_throws_IllegalArgumentException() throws Exception {

    // WHEN login with wrong password THEN exception thrown
    Exception exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> userService.login(EMAIL, "wrong-password"));

    // AND message is "Invalid password"
    assertThat(exception.getMessage()).isEqualTo("Invalid password");
  }
}
