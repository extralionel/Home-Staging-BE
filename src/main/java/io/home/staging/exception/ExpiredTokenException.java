package io.home.staging.exception;

import lombok.Getter;

@Getter
public class ExpiredTokenException extends RuntimeException {

  private final ErrorCode errorCode;

  public ExpiredTokenException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public ExpiredTokenException(String message, Throwable cause, ErrorCode errorCode) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
