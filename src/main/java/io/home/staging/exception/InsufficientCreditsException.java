package io.home.staging.exception;

public class InsufficientCreditsException extends RuntimeException {

  private final ErrorCode errorCode;

  public InsufficientCreditsException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
