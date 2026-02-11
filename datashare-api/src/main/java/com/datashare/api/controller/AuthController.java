package com.datashare.api.controller;

import com.datashare.api.dto.LoginRequest;
import com.datashare.api.dto.LoginResponse;
import com.datashare.api.dto.RegisterRequest;
import com.datashare.api.dto.RegisterResponse;
import com.datashare.api.entities.User;
import com.datashare.api.mapper.UserMapper;
import com.datashare.api.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Handles authentication-related requests and JWT token generation. All endpoints under {@code
 * /auth} are publicly accessible.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final UserService userService;
  private final UserMapper userDtoMapper;

  /**
   * Register a new user.
   *
   * @param requestDto the registration request containing email and password
   * @return a response entity containing registration confirmation and user ID with status 201
   *     CREATED
   */
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest requestDto) {
    RegisterResponse response = userService.register(userDtoMapper.toEntity(requestDto));

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Authenticate a user
   *
   * @param request the login request containing email and password
   * @return a response entity containing the JWT token and expiration time with status 200 OK
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    log.info("Login attempt for email: {}", request.getEmail());
    String token = userService.login(request.getEmail(), request.getPassword());
    ResponseCookie cookie =
        ResponseCookie.from("AUTH-TOKEN", token)
            .httpOnly(true) // Inaccessible for JavaScript
            .secure(true) // HTTPS only
            .sameSite("Strict") // Protection CSRF
            .path("/") // Valid for all the app
            .maxAge(Duration.ofDays(7)) // 7 jours
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    log.info("User {} logged in successfully", request.getEmail());
    log.debug("Cookie created: {}", cookie.toString());
    return ResponseEntity.ok(
        new LoginResponse(
            "Login successful",
            request.getEmail(),
            userService.getAuthorities(request.getEmail())));
  }

  /**
   * Gets the current user
   *
   * @param authentication the authentication from JWT filter
   * @return
   */
  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {

    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User user = (User) authentication.getPrincipal();
    return ResponseEntity.ok(
        Map.of(
            "email", user.getUsername(),
            "authorities", user.getAuthorities()));
  }
}
