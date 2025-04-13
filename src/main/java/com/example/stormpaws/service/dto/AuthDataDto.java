package com.example.stormpaws.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthDataDto {
  private String accessToken;
  private String refreshToken;
}
