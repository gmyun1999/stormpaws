package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.IUserRepository;
import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.service.OAuth.IOAuthProvider;
import com.example.stormpaws.service.OAuth.OAuthProviderFactory;
import com.example.stormpaws.service.dto.AuthDataDTO;
import com.example.stormpaws.service.dto.OAuthUserDTO;
import com.example.stormpaws.service.token.ITokenProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final OAuthProviderFactory oauthProviderFactory;
  private final IUserRepository userRepository;
  private final ITokenProvider tokenProvider;

  public UserService(
      OAuthProviderFactory oauthProviderFactory,
      IUserRepository userRepository,
      ITokenProvider tokenProvider) {
    this.oauthProviderFactory = oauthProviderFactory;
    this.userRepository = userRepository;
    this.tokenProvider = tokenProvider;
  }

  /**
   * @param authServer OAuth 제공자 이름 (예: "google", "kakao")
   * @param code OAuth 인증 코드 (인가 코드)
   * @return accessToken과 refreshToken이 포함된 AuthDataDTO
   * @throws com.example.stormpaws.service.oauth.OAuthException - 인증 서버로부터 토큰 발급 실패 시 - 사용자 정보 조회 실패
   *     시
   */
  public AuthDataDTO login(String authServer, String code) {
    IOAuthProvider provider = oauthProviderFactory.getProvider(authServer);
    String providerAccessToken = provider.getOAuthToken(code);
    OAuthUserDTO oauthUser = provider.getOAuthUser(providerAccessToken);

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

    return new AuthDataDTO(accessToken, refreshToken);
  }

  public AuthDataDTO reIssueAccessToken(String refreshToken) {
    // refresh토큰으로 user id 탐색색
    String userId = tokenProvider.getUserIdFromToken(refreshToken);

    // 탐색한 user id로 유저 모델을 받아 새 accesstoken 생성
    UserModel user = userRepository.findById(userId).get();
    String accessToken = tokenProvider.createAccessToken(user);

    return new AuthDataDTO(accessToken, refreshToken);
  }
}
