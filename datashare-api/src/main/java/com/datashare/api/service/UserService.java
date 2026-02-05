package com.datashare.api.service;

import com.datashare.api.dto.RegisterResponse;
import com.datashare.api.entities.User;
import com.datashare.api.repository.UserRepository;
import com.datashare.api.security.JwtService;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
  public RegisterResponse register(User user) {

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

    return new RegisterResponse(
        "User registered successfully", String.valueOf(user.getEmail()), user.getAuthorities());
  }

  /**
   * Authenticates a user and generates a JWT token.
   *
   * @param email the user's email. Must not be null
   * @param password the user's password. Must not be null
   * @return a JWT token for the authenticated user
   * @throws IllegalArgumentException if the email or password is invalid
   */
  public String login(String email, String password) {

    Assert.notNull(email, "Email must not be null");
    Assert.notNull(password, "Password must not be null");
    log.info("Authenticating user with email: {}", email);

    try {
      UserDetails existingUser = userRepository.loadUserByUsername(email);
      if (passwordEncoder.matches(password, existingUser.getPassword())) {

        return jwtService.generateToken(existingUser);
      }
      throw new IllegalArgumentException("Invalid password");
    } catch (UsernameNotFoundException exception) {
      throw new IllegalArgumentException("Invalid email");
    }
  }

  /**
   * Get the user authorities
   *
   * @param email the user's email. Must not be null
   * @return the user's authorities
   */
  public Collection<?> getAuthorities(String email) {
    Assert.notNull(email, "Email must not be null");
    return userRepository.loadUserByUsername(email).getAuthorities();
  }
}
