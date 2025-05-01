package com.example.stormpaws.web.controller;

import com.example.stormpaws.service.UserService;
import com.example.stormpaws.service.dto.AuthDataDTO;
import com.example.stormpaws.web.dto.ApiResponse;
import com.example.stormpaws.web.dto.OAuthCodeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService authService;

  public UserController(UserService authService) {
    this.authService = authService;
  }

  @GetMapping("/login/{authServer}")
  public ResponseEntity<ApiResponse<AuthDataDTO>> login(
      @PathVariable String authServer, @RequestParam String code) {
    AuthDataDTO authData = authService.login(authServer, code);
    ApiResponse<AuthDataDTO> response = new ApiResponse<>(true, "Login Success", authData);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/getToken/{authServer}")//클라이언트로 JWT토큰 넘겨주기
  public ResponseEntity<ApiResponse<AuthDataDTO>> sendToken(
      @PathVariable("authServer") String authServer, @RequestBody OAuthCodeRequest request) {
    AuthDataDTO authData = authService.login(authServer, request.getCode());
    ApiResponse<AuthDataDTO> response = new ApiResponse<>(true, "Success", authData);
    return ResponseEntity.ok(response);
  }

  // GET /user/me : 현재 인증된 사용자 정보 조회
  // @GetMapping("/me")
  // public ResponseEntity<ApiResponse<?>> getUserInfo() {
  //     // 구현하세요
  // }
}
