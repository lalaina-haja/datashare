package com.datashare.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO returned after a successful authentication.
 *
 * <p>Contains the issued token and information about its expiration.
 */
@Data
public class LoginResponseDTO {
  /** The issued JWT token string. */
  @NotBlank String token;

  /** Token expiration information (string representation, e.g. seconds until expiry). */
  @NotBlank String expiresIn;
}
