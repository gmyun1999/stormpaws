package com.example.stormpaws.infra.external.OAuth;

import com.example.stormpaws.service.OAuth.IOAuthProvider;
import com.example.stormpaws.service.dto.OAuthUser;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    // 안전하게 access_token 추출
    return (String)
        Objects.requireNonNull(response.getBody(), "Token response body is null")
            .get("access_token");
  }

  @Override
  public OAuthUser getOAuthUser(String accessToken) {
    String url = "https://www.googleapis.com/oauth2/v2/userinfo";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, request, new ParameterizedTypeReference<Map<String, Object>>() {});

    Map<String, Object> body =
        Objects.requireNonNull(response.getBody(), "Failed to retrieve user information");

    OAuthUser user = new OAuthUser();
    user.setId((String) body.get("id"));
    user.setName((String) body.get("name"));
    user.setEmail((String) body.get("email"));

    return user;
  }
}
