package io.home.staging.model.response;

import io.home.staging.exception.ErrorCode;

public class ApiResponse {

  private ErrorCode errorCode;
  private String message;
  private int status;
  private String path;

  public ApiResponse() {}

  public ApiResponse(ErrorCode errorCode, String message, int status, String path) {
    this.errorCode = errorCode;
    this.message = message;
    this.status = status;
    this.path = path;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
