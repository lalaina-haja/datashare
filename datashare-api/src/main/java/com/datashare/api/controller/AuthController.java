package com.datashare.api.controller;

import com.datashare.api.dto.LoginRequestDto;
import com.datashare.api.dto.LoginResponseDto;
import com.datashare.api.dto.RegisterRequestDto;
import com.datashare.api.dto.RegisterResponseDto;
import com.datashare.api.mapper.UserMapper;
import com.datashare.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<RegisterResponseDto> register(
      @Valid @RequestBody RegisterRequestDto requestDto) {
    RegisterResponseDto response = userService.register(userDtoMapper.toEntity(requestDto));

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Authenticate a user and generate a JWT token.
   *
   * @param requestDto the login request containing email and password
   * @return a response entity containing the JWT token and expiration time with status 200 OK
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
    LoginResponseDto response = userService.login(requestDto.getEmail(), requestDto.getPassword());
    return ResponseEntity.ok(response);
  }
}
