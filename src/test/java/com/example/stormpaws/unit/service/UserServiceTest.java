package com.example.stormpaws.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.repository.UserRepository;
import com.example.stormpaws.service.UserService;
import com.example.stormpaws.service.dto.AuthDataDTO;
import com.example.stormpaws.service.dto.OAuthUserDTO;
import com.example.stormpaws.service.OAuth.IOAuthProvider;
import com.example.stormpaws.service.OAuth.OAuthProviderFactory;
import com.example.stormpaws.service.token.ITokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private OAuthProviderFactory oauthProviderFactory;

  @Mock private UserRepository userRepository;

  @Mock private ITokenProvider tokenProvider;

  @Mock private IOAuthProvider oauthProvider;

  @InjectMocks private UserService authService;

  private final String authServer = "google";
  private final String code = "sample-code";
  private final String providerAccessToken = "provider-access-token";

  private OAuthUserDTO createSampleOAuthUser(String id, String name, String email) {
    OAuthUserDTO oauthUser = new OAuthUserDTO();
    oauthUser.setId(id);
    oauthUser.setName(name);
    oauthUser.setEmail(email);
    return oauthUser;
  }

  @BeforeEach
  void setup() {
    // 공통: OAuthProviderFactory가 authServer에 대해 oauthProvider를 반환하도록 설정
    when(oauthProviderFactory.getProvider(authServer)).thenReturn(oauthProvider);
    // OAuthProvider에서 getOAuthToken과 getOAuthUser 호출 결과 설정
    when(oauthProvider.getOAuthToken(code)).thenReturn(providerAccessToken);
  }

  @Test
  void testLogin_existingUser() {
    // 기존 사용자 케이스
    String oauthUserId = "existing-oauth-id";
    OAuthUserDTO oauthUser = createSampleOAuthUser(oauthUserId, "John Doe", "john@example.com");
    when(oauthProvider.getOAuthUser(providerAccessToken)).thenReturn(oauthUser);
    when(oauthProvider.getProviderName()).thenReturn("GOOGLE");

    // 기존 사용자가 repo에 존재하는 경우
    UserModel existingUser =
        UserModel.builder()
            .id("existing-id")
            .name(oauthUser.getName())
            .email(oauthUser.getEmail())
            .oauthType("GOOGLE")
            .oauthId(oauthUserId)
            .tosAgreed(true)
            .build();
    when(userRepository.findByOauthTypeAndOauthId("GOOGLE", oauthUserId))
        .thenReturn(Optional.of(existingUser));

    // tokenProvider가 기존 사용자를 전달받아 토큰 발급
    when(tokenProvider.createAccessToken(existingUser)).thenReturn("access-token");
    when(tokenProvider.createRefreshToken(existingUser)).thenReturn("refresh-token");

    // Act
    AuthDataDTO authData = authService.login(authServer, code);

    // Assert
    assertNotNull(authData);
    assertEquals("access-token", authData.getAccessToken());
    assertEquals("refresh-token", authData.getRefreshToken());

    // 기존 사용자이므로 저장(save) 호출은 없음을 검증
    verify(userRepository, never()).save(any(UserModel.class));
  }

  @Test
  void testLogin_newUser() {
    // 신규 사용자 케이스
    String oauthUserId = "new-oauth-id";
    OAuthUserDTO oauthUser = createSampleOAuthUser(oauthUserId, "Jane Doe", "jane@example.com");
    when(oauthProvider.getOAuthUser(providerAccessToken)).thenReturn(oauthUser);
    when(oauthProvider.getProviderName()).thenReturn("GOOGLE");

    // repo에 신규 사용자가 없으므로 empty 반환
    when(userRepository.findByOauthTypeAndOauthId("GOOGLE", oauthUserId))
        .thenReturn(Optional.empty());

    // tokenProvider는 생성되는 새 사용자에 대해 토큰 발급 (어떤 UserModel이 전달되더라도 동일 토큰을 반환하도록 stub)
    when(tokenProvider.createAccessToken(any(UserModel.class))).thenReturn("new-access-token");
    when(tokenProvider.createRefreshToken(any(UserModel.class))).thenReturn("new-refresh-token");

    // Act
    AuthDataDTO authData = authService.login(authServer, code);

    // Assert
    assertNotNull(authData);
    assertEquals("new-access-token", authData.getAccessToken());
    assertEquals("new-refresh-token", authData.getRefreshToken());

    // 신규 사용자의 경우 save()가 호출되어 저장됨을 검증
    verify(userRepository, times(1))
        .save(
            argThat(
                new ArgumentMatcher<UserModel>() {
                  @Override
                  public boolean matches(UserModel user) {
                    // 신규 생성된 유저의 필드 값이 OAuthUser와 OAuthProvider의 정보에 맞게 설정되어야 함
                    return user.getName().equals("Jane Doe")
                        && user.getEmail().equals("jane@example.com")
                        && user.getOauthType().equals("GOOGLE")
                        && user.getOauthId().equals(oauthUserId)
                        && user.isTosAgreed();
                  }
                }));
  }
}
