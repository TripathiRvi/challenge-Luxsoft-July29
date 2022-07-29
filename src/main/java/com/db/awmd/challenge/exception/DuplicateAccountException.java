package com.db.awmd.challenge.exception;

public class DuplicateAccountException extends RuntimeException {

  public DuplicateAccountException(String message) {
    super(message);
  }
}
