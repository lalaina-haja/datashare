package com.datashare.api.handler;

/**
 * Exception thrown when a requested resource is not found.
 *
 * <p>This is a runtime exception that typically results in a 404 HTTP response.
 */
public class ResourceNotFoundException extends RuntimeException {
  /**
   * Constructs a ResourceNotFoundException with the specified detail message.
   *
   * @param message the detail message
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
