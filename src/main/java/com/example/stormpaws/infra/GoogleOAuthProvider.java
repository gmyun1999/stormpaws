package com.example.stormpaws.infra;

import com.example.stormpaws.service.OAuth.IOAuthProvider;
import com.example.stormpaws.service.dto.OAuthUserDTO;
import com.example.stormpaws.service.exception.OAuthException;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException; // for exception
import org.springframework.web.client.HttpServerErrorException; // for exception
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOAuthProvider implements IOAuthProvider {

  @Value("${google.client-id}")
  private String clientId;

  @Value("${google.client-secret}")
  private String clientSecret;

  @Value("${google.redirect-uri}")
  private String redirectUri;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String getProviderName() {
    return "google";
  }

  @Override
  public String getOAuthToken(String code) {
    String url = "https://oauth2.googleapis.com/token";

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("redirect_uri", redirectUri);
    params.add("code", code);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});

      return (String)
          Objects.requireNonNull(response.getBody(), "Token response body is null")
              .get("access_token");

    } catch (HttpClientErrorException | HttpServerErrorException e) {
      // Google API에서 반환한 응답 본문을 가져오기
      String errorResponse = e.getResponseBodyAsString();

      // 상세 오류 로그 추가
      System.err.println("Google OAuth token request failed: " + errorResponse);

      // OAuthException에 오류 메시지와 응답 본문 전달
      throw new OAuthException("Google OAuth 토큰 발급 실패", errorResponse);
    }
    catch (Exception e) {
      throw new OAuthException("Google OAuth 토큰 발급 실패", e);
    } 
  }

  @Override
  public OAuthUserDTO getOAuthUser(String accessToken) {
    String url = "https://www.googleapis.com/oauth2/v2/userinfo";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              request,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      Map<String, Object> body =
          Objects.requireNonNull(response.getBody(), "Failed to retrieve user information");

      OAuthUserDTO user = new OAuthUserDTO();
      user.setId((String) body.get("id"));
      user.setName((String) body.get("name"));
      user.setEmail((String) body.get("email"));

      return user;

    } catch (Exception e) {
      throw new OAuthException("Google OAuth 사용자 정보 조회 실패", e);
    }
  }
}
