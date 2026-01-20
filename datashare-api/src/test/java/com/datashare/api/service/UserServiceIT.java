package com.datashare.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.datashare.api.dto.LoginResponseDto;
import com.datashare.api.entities.User;
import com.datashare.api.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
public class UserServiceIT {

  private static final String EMAIL = "name@example.com";
  private static final String PASSWORD = "PASSWORD";

  @MockitoBean private UserRepository userRepository;

  @MockitoBean private PasswordEncoder passwordEncoder;

  @Autowired private Environment environment;

  @Autowired private UserService userService;

  @Test
  public void login_successful_returns_token() {

    // GIVEN the existing user with correct password
    User user = new User(null, EMAIL, PASSWORD, null);
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(any(), any())).thenReturn(true);

    // WHEN user logs in
    LoginResponseDto response = userService.login(EMAIL, PASSWORD);
    String actualToken = response.getToken();
    Long actualExpiresIn = response.getExpiresIn();

    // THEN
    assertThat(actualToken).isNotBlank();
    assertTrue(
        Math.abs(
                Long.parseLong(environment.getProperty("security.jwt.expiration"))
                    - actualExpiresIn)
            <= 1);
  }
}
