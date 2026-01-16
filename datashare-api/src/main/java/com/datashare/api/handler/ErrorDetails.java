package com.datashare.api.handler;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for error response details.
 *
 * <p>Contains error information including timestamp, message, and request details to be sent to
 * clients when exceptions occur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

  /** The timestamp when the error occurred. */
  private LocalDateTime timestamp;

  /** The error message. */
  private String message;

  /** Additional details about the error (e.g., request path). */
  private String details;
}
