package com.datashare.api.service;

import com.datashare.api.dto.LoginResponseDto;
import com.datashare.api.dto.RegisterResponseDto;
import com.datashare.api.entities.User;
import com.datashare.api.repository.UserRepository;
import com.datashare.api.service.security.JwtService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Service for user authentication and management operations.
 *
 * <p>Provides methods for user registration, login authentication, and password management. Handles
 * password encoding and JWT token generation for authenticated users.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Registers a new user in the system.
   *
   * @param user the user to register. Must not be null
   * @return the registered user with encoded password
   * @throws IllegalArgumentException if the email is already in use
   */
  public RegisterResponseDto register(User user) {

    Assert.notNull(user, "User must not be null");
    log.info("Registering user with email: {}", user.getEmail());

    Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
    if (existingUser.isPresent()) {
      throw new IllegalArgumentException("Email is already in use: " + user.getEmail());
    }
    // Encode the user's password before saving
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    // Save the new user to the repository
    userRepository.save(user);

    return new RegisterResponseDto("User registered successfully", String.valueOf(user.getId()));
  }

  /**
   * Authenticates a user and generates a JWT token.
   *
   * @param email the user's email. Must not be null
   * @param password the user's password. Must not be null
   * @return a JWT token for the authenticated user
   * @throws IllegalArgumentException if the email or password is invalid
   */
  public LoginResponseDto login(String email, String password) {

    Assert.notNull(email, "Email must not be null");
    Assert.notNull(password, "Password must not be null");
    log.info("Authenticating user with email: {}", email);

    Optional<User> existingUser = userRepository.findByEmail(email);
    if (existingUser.isPresent()
        && passwordEncoder.matches(password, existingUser.get().getPassword())) {
      UserDetails userDetails =
          org.springframework.security.core.userdetails.User.builder()
              .username(existingUser.get().getEmail())
              .password(existingUser.get().getPassword())
              .build();
      String token = jwtService.generateToken(userDetails);
      return new LoginResponseDto(
          token, jwtService.getExpiresAt(token), jwtService.getExpiresIn(token));
    }
    throw new IllegalArgumentException("Invalid email or password");
  }
}
