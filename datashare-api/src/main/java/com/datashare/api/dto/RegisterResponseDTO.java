package com.datashare.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO returned after a successful user registration.
 *
 * <p>Contains a human-readable message and the created user's identifier.
 */
@Data
public class RegisterResponseDTO {
  /** Informational message about the registration result. */
  @NotBlank String message;

  /** Identifier of the newly created user (string representation). */
  @NotBlank String userId;
}
