package com.example.stormpaws.service;

import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.repository.UserRepository;
import com.example.stormpaws.service.OAuth.IOAuthProvider;
import com.example.stormpaws.service.OAuth.OAuthProviderFactory;
import com.example.stormpaws.service.dto.AuthDataDto;
import com.example.stormpaws.service.dto.OAuthUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final OAuthProviderFactory oauthProviderFactory;
  private final UserRepository userRepository;
  private final ITokenProvider tokenProvider;

  public AuthService(
      OAuthProviderFactory oauthProviderFactory,
      UserRepository userRepository,
      ITokenProvider tokenProvider) {
    this.oauthProviderFactory = oauthProviderFactory;
    this.userRepository = userRepository;
    this.tokenProvider = tokenProvider;
  }

  public AuthDataDto login(String authServer, String code) {
    IOAuthProvider provider = oauthProviderFactory.getProvider(authServer);
    String providerAccessToken = provider.getOAuthToken(code);
    OAuthUser oauthUser = provider.getOAuthUser(providerAccessToken);

    // oauthType 및 oauthId로 기존 사용자 검색
    Optional<UserModel> optionalUser =
        userRepository.findByOauthTypeAndOauthId(provider.getProviderName(), oauthUser.getId());
    UserModel user;

    if (optionalUser.isPresent()) {
      user = optionalUser.get();
    } else {
      user =
          UserModel.builder()
              .id(UUID.randomUUID().toString())
              .name(oauthUser.getName())
              .email(oauthUser.getEmail())
              .oauthType(provider.getProviderName())
              .oauthId(oauthUser.getId())
              .tosAgreed(true)
              .build();
      userRepository.save(user);
    }

    // JWT 토큰 발급
    String accessToken = tokenProvider.createAccessToken(user);
    String refreshToken = tokenProvider.createRefreshToken(user);

    return new AuthDataDto(accessToken, refreshToken);
  }
}
