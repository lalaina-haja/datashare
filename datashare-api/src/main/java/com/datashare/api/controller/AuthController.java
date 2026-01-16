package com.datashare.api.controller;

import com.datashare.api.service.security.JwtService;
import lombok.RequiredArgsConstructor;
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

  private final JwtService jwtService;

  // TODO: Implement authentication endpoints (e.g., register, login, token
  // refresh)
}
