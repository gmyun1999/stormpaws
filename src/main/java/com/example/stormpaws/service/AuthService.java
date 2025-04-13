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
  private final TokenService tokenService;

  public AuthService(
      OAuthProviderFactory oauthProviderFactory,
      UserRepository userRepository,
      TokenService tokenService) {
    this.oauthProviderFactory = oauthProviderFactory;
    this.userRepository = userRepository;
    this.tokenService = tokenService;
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
      // 신규 사용자 생성
      user = new UserModel();
      user.setId(UUID.randomUUID().toString());
      user.setName(oauthUser.getName());
      user.setEmail(oauthUser.getEmail());
      user.setOauthType(provider.getProviderName());
      user.setOauthId(oauthUser.getId());
      user.setTosAgreed(true);
      user = userRepository.save(user);
    }

    // JWT 토큰 발급
    String accessToken = tokenService.generateAccessToken(user);
    String refreshToken = tokenService.generateRefreshToken(user);

    return new AuthDataDto(accessToken, refreshToken);
  }
}
