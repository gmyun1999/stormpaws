package com.example.stormpaws.service.token;

import com.example.stormpaws.domain.model.UserModel;
import io.jsonwebtoken.Claims;

public interface ITokenProvider {
  /** UserModel 기반의 Access Token을 생성한다. */
  String createAccessToken(UserModel user);

  /** UserModel 기반의 Refresh Token을 생성한다. */
  String createRefreshToken(UserModel user);

  /** JWT 토큰을 파싱하여 Claims 객체를 반환한다. */
  Claims parseToken(String token);

  /** JWT 토큰의 유효성을 검증한다. */
  boolean validateToken(String token);

  /** JWT 토큰에서 사용자 ID(subject)를 추출한다. */
  String getUserIdFromToken(String token);
}
