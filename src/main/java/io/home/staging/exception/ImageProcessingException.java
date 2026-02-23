package io.home.staging.exception;

public class ImageProcessingException extends RuntimeException {

  private final ErrorCode errorCode;

  public ImageProcessingException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public ImageProcessingException(String message, Throwable cause, ErrorCode errorCode) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
