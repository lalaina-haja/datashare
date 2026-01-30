package com.datashare.api.dto;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for registration responses.
 *
 * <p>Contains a message indicating the result of the registration process and the unique identifier
 * of the newly created user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

  /** A message indicating the result of the registration process. */
  String message;

  /** User's email. */
  String email;

  /** User authorities */
  Collection<?> authorities;
}
