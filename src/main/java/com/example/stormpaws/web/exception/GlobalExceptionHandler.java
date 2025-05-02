package com.example.stormpaws.web.exception;

import com.example.stormpaws.service.exception.OAuthException;
import com.example.stormpaws.web.dto.response.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(Exception ex) {
    BindingResult result =
        ex instanceof MethodArgumentNotValidException
            ? ((MethodArgumentNotValidException) ex).getBindingResult()
            : ((BindException) ex).getBindingResult();

    Map<String, String> errors = new LinkedHashMap<>();
    result.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

    return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Validation failed", errors));
  }
}
