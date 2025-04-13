package com.example.stormpaws.service.OAuth;

import com.example.stormpaws.service.dto.OAuthUser;

public interface IOAuthProvider {
  String getProviderName();

  String getOAuthToken(String code);

  OAuthUser getOAuthUser(String accessToken);
}
