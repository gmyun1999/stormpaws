package com.example.stormpaws.service.exception;

public class OAuthException extends RuntimeException {

  private String errorResponse;

  public OAuthException(String message) {
    super(message);
  }

  public OAuthException(String message, Throwable cause) {
    super(message, cause);
  }

  // 오류 응답을 포함하는 생성자
  public OAuthException(String message, String errorResponse) {
    super(message);

    this.errorResponse = errorResponse; // Google API에서 반환한 에러 응답을 저장
  }

  // 오류 응답을 반환하는 getter

  public String getErrorResponse() {
    return errorResponse;
  }
}
