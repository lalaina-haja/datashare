package com.datashare.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datashare.api.dto.LoginResponseDto;
import com.datashare.api.dto.RegisterResponseDto;
import com.datashare.api.entities.User;
import com.datashare.api.repository.UserRepository;
import com.datashare.api.service.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for {@link UserService}.
 *
 * <p>Tests cover user registration and authentication scenarios, including validation, error
 * handling, and successful operations.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  private static final String EMAIL = "name@example.com";
  private static final String PASSWORD = "PASSWORD";
  private static final String TOKEN = "TOKEN";
  private static final Long EXPIRATION = 3600L;

  @InjectMocks private UserService userService;

  @Mock private JwtService jwtService;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  /** Test that registering a null user throws IllegalArgumentException. */
  @Test
  public void register_null_user_throws_IllegalArgumentException() {

    // WHEN register with null user
    Exception exception =
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(null));

    // THEN expect exception thrown with message "User must not be null"
    assertThat(exception.getMessage()).isEqualTo("User must not be null");
  }

  /**
   * Test that registering a user with an already existing email throws IllegalArgumentException.
   */
  @Test
  public void register_already_existing_user_throws_IllegalArgumentException() {

    // GIVEN the existing user
    User user = new User(null, EMAIL, PASSWORD, null);
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

    // WHEN register with existing user
    Exception exception =
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(user));

    // THEN expect exception thrown with message "Email is already in use: ..."
    assertThat(exception.getMessage()).isEqualTo("Email is already in use: " + user.getEmail());
  }

  /** Test that registering a new user successfully encodes the password and saves the user. */
  @Test
  public void register_user_successful() {

    // GIVEN the user does not exist
    User user = new User(null, EMAIL, PASSWORD, null);
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn(PASSWORD);
    // when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // WHEN registering the user
    RegisterResponseDto response = userService.register(user);

    // THEN the user is saved in the repository with encoded password
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue()).isEqualTo(user);
    assertThat(response.getMessage()).isEqualTo("User registered successfully");
    assertThat(response.getUserId()).isEqualTo(String.valueOf(user.getId()));
  }

  /**
   * Test that logging in with null email or password throws IllegalArgumentException.
   *
   * @param missing the field that is missing ("email" or "password")
   */
  @ParameterizedTest(name = "login_with_missing_{0}_field_throws_IllegalArgumentException")
  @ValueSource(strings = {"email", "password"})
  public void login_with_missing_field_throws_IllegalArgumentException(String missing) {

    // WHEN login with missing field
    Exception exception =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                userService.login(
                    missing.equals("email") ? null : "e@mail.com",
                    missing.equals("password") ? null : "dupont"));

    // THEN return exception with appropriate message
    assertThat(exception.getMessage())
        .isEqualTo((missing.equals("email") ? "Email" : "Password") + " must not be null");
  }

  /** Test that logging in with an unknown email throws IllegalArgumentException. */
  @Test
  public void login_unknown_user_throws_IllegalArgumentException() {

    // GIVEN unknown user
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    // WHEN login THEN exception thrown
    Exception exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> userService.login("unknown", PASSWORD));

    // THEN
    assertThat(exception.getMessage()).isEqualTo("Invalid email or password");
  }

  /** Test that logging in with a wrong password throws IllegalArgumentException. */
  @Test
  public void login_wrong_password_throws_IllegalArgumentException() {

    // GIVEN user exists and a wrong password
    User user = new User(null, EMAIL, PASSWORD, null);
    when(passwordEncoder.matches(any(), any())).thenReturn(false);
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

    // WHEN passing wrong PASSWORD THEN exception thrown
    Exception exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> userService.login(EMAIL, "wrong"));

    // THEN
    verify(passwordEncoder).matches("wrong", PASSWORD);
    assertThat(exception.getMessage()).isEqualTo("Invalid email or password");
  }

  /** Test that logging in with correct credentials successfully returns a JWT token. */
  @Test
  public void login_successful_returns_token() {
    // GIVEN correct login and password
    User user = new User(null, EMAIL, PASSWORD, null);
    Instant expiresAt = Instant.now().plusSeconds(EXPIRATION);
    when(passwordEncoder.matches(any(), any())).thenReturn(true);
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
    when(jwtService.generateToken(any())).thenReturn(TOKEN);
    when(jwtService.getExpiresIn(any())).thenReturn(EXPIRATION);
    when(jwtService.getExpiresAt(any())).thenReturn(expiresAt);

    // WHEN
    LoginResponseDto response = userService.login(EMAIL, PASSWORD);
    String actualToken = response.getToken();
    Long actualExpiresIn = response.getExpiresIn();
    Instant actualExpiresAt = response.getExpiresAt();

    // THEN
    assertThat(actualToken).isEqualTo(TOKEN);
    assertThat(actualExpiresIn).isEqualTo(EXPIRATION);
    assertThat(actualExpiresAt).isEqualTo(expiresAt);
  }
}
