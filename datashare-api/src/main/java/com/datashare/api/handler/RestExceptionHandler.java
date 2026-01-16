package com.datashare.api.handler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for REST endpoints.
 *
 * <p>This class provides centralized exception handling for the REST API, converting various types
 * of exceptions into appropriate HTTP responses with error details. It extends Spring's
 * ResponseEntityExceptionHandler to handle common Spring exceptions as well.
 *
 * <p>The handler maps different exception types to their corresponding HTTP status codes: -
 * IllegalArgumentException and IllegalStateException → 400 Bad Request - ResourceNotFoundException
 * → 404 Not Found - BadCredentialsException → 401 Unauthorized - AccessDeniedException → 403
 * Forbidden - General Exception → 500 Internal Server Error
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handles IllegalArgumentException and IllegalStateException.
   *
   * <p>Returns a 400 Bad Request response with error details when these exceptions occur. These
   * exceptions typically indicate invalid input or invalid state conditions.
   *
   * @param runtimeException the caught exception (either IllegalArgumentException or
   *     IllegalStateException)
   * @param request the current web request
   * @return a ResponseEntity with error details and 400 status code
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
  protected ResponseEntity<Object> handleConflict(
      RuntimeException runtimeException, WebRequest request) {
    logError(runtimeException);
    return handleExceptionInternal(
        runtimeException,
        getErrorDetails(runtimeException, request),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        request);
  }

  /**
   * Handles ResourceNotFoundException.
   *
   * <p>Returns a 404 Not Found response with error details when a requested resource is not found.
   *
   * @param runtimeException the ResourceNotFoundException caught
   * @param request the current web request
   * @return a ResponseEntity with error details and 404 status code
   */
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(value = {ResourceNotFoundException.class})
  protected ResponseEntity<Object> handleNotFound(
      RuntimeException runtimeException, WebRequest request) {
    logError(runtimeException);
    return handleExceptionInternal(
        runtimeException,
        getErrorDetails(runtimeException, request),
        new HttpHeaders(),
        HttpStatus.NOT_FOUND,
        request);
  }

  /**
   * Handles BadCredentialsException.
   *
   * <p>Returns a 401 Unauthorized response with error details when authentication fails (e.g.,
   * invalid credentials).
   *
   * @param badCredentialsException the caught BadCredentialsException
   * @param request the current web request
   * @return a ResponseEntity with error details and 401 status code
   */
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(value = {BadCredentialsException.class})
  protected ResponseEntity<Object> handleBadCredentialsException(
      BadCredentialsException badCredentialsException, WebRequest request) {
    logError(badCredentialsException);
    return handleExceptionInternal(
        badCredentialsException,
        getErrorDetails(badCredentialsException, request),
        new HttpHeaders(),
        HttpStatus.UNAUTHORIZED,
        request);
  }

  /**
   * Handles AccessDeniedException.
   *
   * <p>Returns a 403 Forbidden response with error details when the user lacks sufficient
   * permissions to access the requested resource.
   *
   * @param accessDeniedException the caught AccessDeniedException
   * @param request the current web request
   * @return a ResponseEntity with error details and 403 status code
   */
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(value = {AccessDeniedException.class})
  protected ResponseEntity<Object> handleForbiddenException(
      AccessDeniedException accessDeniedException, WebRequest request) {
    logError(accessDeniedException);
    return handleExceptionInternal(
        accessDeniedException,
        getErrorDetails(accessDeniedException, request),
        new HttpHeaders(),
        HttpStatus.FORBIDDEN,
        request);
  }

  /**
   * Handles all other exceptions not specifically handled by other handlers.
   *
   * <p>Returns a 500 Internal Server Error response as a fallback for unexpected exceptions.
   *
   * @param runtimeException the caught exception
   * @param request the current web request
   * @return a ResponseEntity with a generic error message and 500 status code
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(value = {Exception.class})
  protected ResponseEntity<Object> handleException(
      RuntimeException runtimeException, WebRequest request) {
    logError(runtimeException);
    return handleExceptionInternal(
        runtimeException,
        "Internal Server error",
        new HttpHeaders(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }

  /**
   * Logs exception details to the error logger.
   *
   * @param exception the exception to log
   */
  private void logError(Exception exception) {
    logger.error(exception.getMessage(), exception);
  }

  /**
   * Creates error details containing timestamp, message, and request path.
   *
   * @param exception the exception from which to extract the error message
   * @param request the current web request containing the request path
   * @return an ErrorDetails object with error information
   */
  private ErrorDetails getErrorDetails(Exception exception, WebRequest request) {
    return new ErrorDetails(
        LocalDateTime.now(), exception.getMessage(), request.getDescription(false));
  }
}
