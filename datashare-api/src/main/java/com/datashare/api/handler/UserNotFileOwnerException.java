package com.datashare.api.handler;

/** Exception thrown when a user is not the file owner */
public class UserNotFileOwnerException extends Exception {

  public UserNotFileOwnerException(String message) {
    super(message);
  }
}
