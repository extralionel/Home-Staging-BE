package io.home.staging.exception.handler;

import io.home.staging.exception.ErrorCode;
import io.home.staging.exception.ImageProcessingException;
import io.home.staging.exception.InsufficientCreditsException;
import io.home.staging.model.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ImageProcessingException.class)
  public ResponseEntity<ApiResponse> handleImageProcessing(ImageProcessingException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(new ApiResponse(ex.getErrorCode(), ex.getMessage(), status.value(), request.getRequestURI()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse> handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE;
    return ResponseEntity.status(status)
        .body(new ApiResponse(ErrorCode.FILE_TOO_LARGE, "File size exceeded.", status.value(), request.getRequestURI()));
  }

  @ExceptionHandler(InsufficientCreditsException.class)
  public ResponseEntity<ApiResponse> projectInitException(InsufficientCreditsException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_ACCEPTABLE;
    return ResponseEntity.status(status)
        .body(new ApiResponse(ex.getErrorCode(), ex.getMessage(), status.value(), request.getRequestURI()));
  }
}
