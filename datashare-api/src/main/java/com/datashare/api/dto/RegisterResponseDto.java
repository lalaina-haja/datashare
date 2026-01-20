package com.datashare.api.dto;

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
public class RegisterResponseDto {

  /** A message indicating the result of the registration process. */
  String message;

  /** The unique identifier of the newly created user. */
  String userId;
}
