package com.example.stormpaws.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthDataDto {
  private String accessToken;
  private String refreshToken;
}
