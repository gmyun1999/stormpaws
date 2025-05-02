package com.example.stormpaws.web.controller;

import com.example.stormpaws.service.UserService;
import com.example.stormpaws.service.dto.AuthDataDTO;
import com.example.stormpaws.web.dto.OAuthCodeRequest;
import com.example.stormpaws.web.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService authService;

  public UserController(UserService authService) {
    this.authService = authService;
  }

  @PostMapping("/login/{authServer}") // 클라이언트로 JWT토큰 넘겨주기
  public ResponseEntity<ApiResponse<AuthDataDTO>> sendToken(
      @PathVariable("authServer") String authServer, @RequestBody OAuthCodeRequest request) {
    AuthDataDTO authData = authService.login(authServer, request.getCode());
    ApiResponse<AuthDataDTO> response = new ApiResponse<>(true, "Success", authData);
    return ResponseEntity.ok(response);
  }

  //   @GetMapping("/me")
  //   public ResponseEntity<ApiResponse<OAuthUserDTO>> getUserInfo(
  //     @AuthenticationPrincipal CustomUserDetails userDetails
  //   ) {
  //     // db에서 직접 가져오는게 맞는지, 아니면 UserDetails에서 가져오는게 맞는지 고민해보세요
  //   }
  // }
}
