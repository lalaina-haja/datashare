package com.datashare.api.handler;

import com.datashare.api.dto.ApiErrorDto;
import com.datashare.api.dto.ValidationErrorDto;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for REST controllers.
 *
 * <p>Handles various exceptions thrown during API request processing and returns structured error
 * responses with appropriate HTTP status codes.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handles IllegalArgumentException and IllegalStateException.
   *
   * <p>Returns a 400 Bad Request response with error details when invalid input or state is
   * detected.
   *
   * @param runtimeException the caught exception
   * @param request the current web request
   * @return a ResponseEntity with error details and 400 status code
   */
  @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
  protected ResponseEntity<Object> handleConflict(
      RuntimeException runtimeException, WebRequest request) {

    logError(runtimeException);

    return handleExceptionInternal(
        runtimeException,
        ApiErrorDto.of(HttpStatus.BAD_REQUEST.value(), runtimeException.getMessage(), request),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        request);
  }

  @Override
  protected ResponseEntity<Object> handleNoResourceFoundException(
      NoResourceFoundException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    logError(exception);

    return handleExceptionInternal(
        exception,
        ApiErrorDto.of(HttpStatus.NOT_FOUND.value(), exception.getMessage(), request),
        new HttpHeaders(),
        HttpStatus.NOT_FOUND,
        request);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      NoHandlerFoundException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    logError(exception);

    return handleExceptionInternal(
        exception,
        ApiErrorDto.of(HttpStatus.NOT_FOUND.value(), exception.getMessage(), request),
        new HttpHeaders(),
        HttpStatus.NOT_FOUND,
        request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    exception
        .getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    logError(exception);

    return ResponseEntity.badRequest().body(ValidationErrorDto.of(errors, request));
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    logError(exception);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiErrorDto.of(HttpStatus.BAD_REQUEST.value(), "Invalid request content", request));
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
        ApiErrorDto.of(
            HttpStatus.UNAUTHORIZED.value(), badCredentialsException.getMessage(), request),
        new HttpHeaders(),
        HttpStatus.UNAUTHORIZED,
        request);
  }

  /**
   * Handles AccessDeniedException.
   *
   * <p>Returns a 403 Forbidden response with error details when access to a resource is denied.
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
        ApiErrorDto.of(HttpStatus.FORBIDDEN.value(), accessDeniedException.getMessage(), request),
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
        ApiErrorDto.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server error",
            request.getDescription(false).replace("uri=", "")),
        new HttpHeaders(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }

  /**
   * Logs es details to the error logger.
   *
   * @param exception the exception to log
   */
  private void logError(Exception exception) {
    logger.error(exception.getMessage(), exception);
  }
}
