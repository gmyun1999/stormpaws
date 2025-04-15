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

  // POST /user/login/{authServer} : OAuth를 통한 로그인 처리
  @PostMapping("/login/{authServer}")
  public ResponseEntity<ApiResponse<AuthDataDTO>> login(
      @PathVariable String authServer, @Valid @RequestBody OAuthCodeRequest authCodeRequest) {
    AuthDataDTO authData = authService.login(authServer, authCodeRequest.getCode());
    ApiResponse<AuthDataDTO> response = new ApiResponse<>(true, "Login successful", authData);
    return ResponseEntity.ok(response);
  }

  // GET /user/me : 현재 인증된 사용자 정보 조회
  // @GetMapping("/me")
  // public ResponseEntity<ApiResponse<?>> getUserInfo() {
  //     // 구현하세요
  // }
}
