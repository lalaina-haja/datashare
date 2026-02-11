package com.datashare.api.handler;

/** Exception thrown when a download token is not invalid */
public class InvalidTokenException extends Exception {
  public InvalidTokenException(String message) {
    super(message);
  }
}
