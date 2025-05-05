package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.infra.security.CustomUserDetails;
import com.example.stormpaws.service.UserService;
import com.example.stormpaws.service.dto.AuthDataDTO;
import com.example.stormpaws.service.dto.OAuthUserDTO;
import com.example.stormpaws.web.dto.OAuthCodeRequest;
import com.example.stormpaws.web.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<OAuthUserDTO>> getUserInfo(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    // db를 조회해서 만든 객체를 이용해야 함. db를 또 조회하는건 불필요.
    // 1. @AuthenticationPrincipal 로 주입받은 userDetails 에서 UserModel 가져오기
    UserModel currentUser = userDetails.getUser();

    // 2. UserModel 정보를 사용하여 새로운 OAuthUserDTO 객체 생성 및 값 설정
    OAuthUserDTO userInfo = new OAuthUserDTO();
    userInfo.setId(currentUser.getId());
    userInfo.setName(currentUser.getName());
    userInfo.setEmail(currentUser.getEmail());

    // 3. ApiResponse 로 감싸서 성공 응답 반환
    ApiResponse<OAuthUserDTO> response = new ApiResponse<>(true, "Success", userInfo);
    return ResponseEntity.ok(response);
  }
}
