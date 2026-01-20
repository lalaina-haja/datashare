package com.datashare.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class RegisterRequestDto {

  /** The user's email address. Must be a valid email format. */
  @Email(message = "Invalid email format")
  @NotBlank(message = "Email is required")
  String email;

  /** The user's password. Must be at least 8 characters long. */
  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  String password;
}
