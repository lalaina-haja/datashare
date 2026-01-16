package com.datashare.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object for authentication requests.
 *
 * <p>Contains the credentials provided by a user during authentication (login) operations and
 * registration.
 */
@Data
public class AuthUserDTO {
  /** User's login email (must not be blank). */
  @NotBlank String email;

  /** User's password (must not be blank). */
  @NotBlank String password;
}
