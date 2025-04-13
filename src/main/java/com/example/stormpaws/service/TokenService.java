package com.example.stormpaws.service;

import com.example.stormpaws.domain.model.UserModel;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  public String generateAccessToken(UserModel user) {
    // 실제 JWT 생성 로직으로 대체 필요
    return "access-token-for-" + user.getId();
  }

  public String generateRefreshToken(UserModel user) {
    // 실제 JWT 생성 로직으로 대체 필요
    return "refresh-token-for-" + user.getId();
  }
}
