package com.example.stormpaws.service.OAuth;

import com.example.stormpaws.service.dto.OAuthUserDTO;

public interface IOAuthProvider {
  String getProviderName();

  String getOAuthToken(String code);

  OAuthUserDTO getOAuthUser(String accessToken);
}
