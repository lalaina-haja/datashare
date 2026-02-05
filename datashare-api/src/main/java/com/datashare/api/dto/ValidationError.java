package com.datashare.api.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Data Transfer Object for Validation error responses.
 *
 * <p>Contains details about a validation error that occurred during API request processing,
 * including the HTTP status code, error message, errors, request path, and timestamp.
 *
 * @param status the HTTP status code of the error response
 * @param message the error message describing what went wrong
 * @param errors the list of validation errors
 * @param path the request path that caused the error
 * @param timestamp the date and time when the error occurred
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

  /** The HTTP status code of the error response */
  int status;

  /** The error message describing what went wrong */
  String message;

  /** List of validation errors */
  Map<String, String> errors;

  /** The request path that caused the error */
  String path;

  /** The date and time when the error occurred */
  LocalDateTime timestamp;

  /**
   * Creates an ApiErrorDto with the specified parameters.
   *
   * @param status the HTTP status code
   * @param message the error message
   * @param path the request path
   * @param timestamp the error timestamp
   * @return a new ApiErrorDto instance
   */
  public static ValidationError of(Map<String, String> errors, WebRequest request) {
    return new ValidationError(
        HttpStatus.BAD_REQUEST.value(),
        "Validation failed",
        errors,
        request.getDescription(false).replace("uri=", ""),
        LocalDateTime.now());
  }
}
