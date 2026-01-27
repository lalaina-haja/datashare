package com.datashare.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for login responses.
 *
 * <p>Contains the login message and his email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  /** Login message */
  String message;

  /** User email */
  String email;
}
