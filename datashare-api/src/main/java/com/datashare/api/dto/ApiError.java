package com.datashare.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.WebRequest;

/**
 * Data Transfer Object for API error responses.
 *
 * <p>Contains details about an error that occurred during API request processing, including the
 * HTTP status code, error message, request path, and timestamp.
 *
 * <p>Provides factory methods to easily create instances from various sources.
 *
 * @param status the HTTP status code of the error response
 * @param message the error message describing what went wrong
 * @param path the request path that caused the error
 * @param timestamp the date and time when the error occurred
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

  /** The HTTP status code of the error response */
  int status;

  /** The error message describing what went wrong */
  String message;

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
   * @return a new ApiErrorDto instance with current timestamp
   */
  public static ApiError of(int status, String message, String path) {
    return new ApiError(status, message, path, LocalDateTime.now());
  }

  /**
   * Creates an ApiErrorDto from a WebRequest.
   *
   * @param status the HTTP status code
   * @param message the error message
   * @param request the WebRequest object
   * @return a new ApiErrorDto instance with the request URI and current timestamp
   */
  public static ApiError of(int status, String message, WebRequest request) {
    return ApiError.of(status, message, request.getDescription(false).replace("uri=", ""));
  }
}
