package com.example.stormpaws.web.controller;

import com.example.stormpaws.service.UserService;
import com.example.stormpaws.service.dto.AuthDataDTO;
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

  @GetMapping("/login/{authServer}")
  public ResponseEntity<ApiResponse<AuthDataDTO>> login(
      @PathVariable String authServer, @RequestParam String code) {
    AuthDataDTO authData = authService.login(authServer, code);
    ApiResponse<AuthDataDTO> response = new ApiResponse<>(true, "Login Success", authData);
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
