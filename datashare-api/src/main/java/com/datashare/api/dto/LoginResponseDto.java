package com.datashare.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for login responses.
 *
 * <p>Contains the JWT token and its expiration information returned upon successful authentication.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

  /** The JWT bearer token for authentication. */
  String token;

  /** The expiration time of the token. */
  Instant expiresAt;

  /** The expiration duration of the token. */
  Long expiresIn;
}
