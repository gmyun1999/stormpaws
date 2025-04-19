package com.example.stormpaws.web.controller;

import com.example.stormpaws.service.AuthService;
import com.example.stormpaws.service.dto.AuthDataDto;
import com.example.stormpaws.web.dto.ApiResponse;
import com.example.stormpaws.web.dto.OAuthCodeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/user/login")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  // GET  /login/{authServer}
  @GetMapping("/{authServer}")
  public ResponseEntity<ApiResponse<AuthDataDto>> oauthCallback(@PathVariable String authServer, @RequestParam String code){
    AuthDataDto authData = authService.login(authServer, code);
    ApiResponse<AuthDataDto> response = new ApiResponse<>(true, "Login Success", authData);
    return ResponseEntity.ok(response);
  }
  

  // POST /login/{authServer}
  @PostMapping("/{authServer}")
  public ResponseEntity<ApiResponse<AuthDataDto>> login(
      @PathVariable String authServer, @Valid @RequestBody OAuthCodeRequest authCodeRequest) {
    AuthDataDto authData = authService.login(authServer, authCodeRequest.getCode());
    ApiResponse<AuthDataDto> response = new ApiResponse<>(true, "Login successful", authData);
    return ResponseEntity.ok(response);
  }
}
