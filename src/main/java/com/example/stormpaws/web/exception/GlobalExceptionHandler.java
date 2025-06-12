package com.example.stormpaws.web.exception;

import com.example.stormpaws.service.exception.OAuthException;
import com.example.stormpaws.web.dto.response.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

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

  /**
   * @PreAuthorize 표현식 평가에 실패했을 때 던져지는 예외 (인증된 사용자만 접근 가능할 때, 인증은 했으나 권한이 없으면)
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN) // 403
        .body(new ApiResponse<>(false, "Access is denied", null));
  }

  /** 인증 토큰이 없거나(또는 잘못된 토큰) 인증 처리에 실패했을 때 발생 */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
        .body(new ApiResponse<>(false, "Authentication required", null));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, Object> handleNotFound(NoHandlerFoundException ex) {
    return Map.of("success", false, "message", "API not found", "status", 404);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleUnhandledExceptions(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiResponse<>(false, "Service not available", null));
  }
}
