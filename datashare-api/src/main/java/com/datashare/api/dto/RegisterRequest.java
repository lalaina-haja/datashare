package com.datashare.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for registration requests.
 *
 * <p>Contains the user's email and password credentials for account creation. Both fields are
 * validated to ensure they are present and meet required criteria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  /** The user's email address. Must be a valid email format. */
  @NotBlank(message = "Email is required")
  // Email must be valid and contain only alphanumeric characters, dots, hyphens,
  // underscores, and plus signs
  @Email(
      regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
      message = "Invalid email format")
  // Email contains encoded characters
  @Pattern(regexp = "^(?!.*(%[0-9A-Fa-f]{2})).*$", message = "Invalid email format")
  // Email contains invalid characters
  @Pattern(regexp = "^[^<>\"'%;()&\\\\]*$", message = "Invalid email format")
  @Size(max = 255, message = "Email is too long")
  String email;

  /** The user's password. Must be at least 8 characters long. */
  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
      message =
          "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
  String password;
}
