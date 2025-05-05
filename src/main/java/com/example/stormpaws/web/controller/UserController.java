package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.infra.security.CustomUserDetails;
import com.example.stormpaws.service.UserService;
import com.example.stormpaws.service.dto.AuthDataDTO;
import com.example.stormpaws.service.dto.OAuthUserDTO;
import com.example.stormpaws.service.token.ITokenProvider;

import jakarta.validation.Valid;
import com.example.stormpaws.web.dto.OAuthCodeRequest;
import com.example.stormpaws.web.dto.request.JwtRefreshTokenRequest;
import com.example.stormpaws.web.dto.response.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService authService;
  
  @Autowired
  private ITokenProvider tokenService;

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

  // 클라이언트로부터 refresh토큰을 받아 다시 access 토큰 발급하기기
  @PostMapping("/refreshToken")
  public ResponseEntity<ApiResponse<AuthDataDTO>> reissueAccess(
      @RequestBody JwtRefreshTokenRequest request) {
    if (tokenService.validateToken(request.getRefreshToken())) { // 토큰이 유효하다면
      AuthDataDTO authData = authService.reIssueAccessToken(request.getRefreshToken());
      ApiResponse<AuthDataDTO> response = new ApiResponse<>(true, "Success", authData);
      return ResponseEntity.ok(response);
    } else {
      ApiResponse<AuthDataDTO> errorResponse = new ApiResponse<>(false, "Refresh expired", null);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
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
