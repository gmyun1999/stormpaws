package com.example.stormpaws.web.exception;

import com.example.stormpaws.service.exception.OAuthException;
import com.example.stormpaws.web.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OAuthException.class)
  public ResponseEntity<ApiResponse<?>> handleOAuthException(OAuthException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
        .body(new ApiResponse<>(false, ex.getMessage(), null));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.badRequest() // 400
        .body(new ApiResponse<>(false, ex.getMessage(), null));
  }
}
