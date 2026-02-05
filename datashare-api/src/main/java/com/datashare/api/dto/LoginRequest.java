package com.datashare.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for login requests.
 *
 * <p>Contains the user's email and password credentials for authentication. Both fields are
 * validated to ensure they are present and valid.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  /** The user's email address. Must be a valid email format. */
  @NotBlank(message = "Email is required")
  String email;

  /** The user's password. Must not be blank. */
  @NotBlank(message = "Password is required")
  String password;
}
