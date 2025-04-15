package com.example.stormpaws.unit.infra.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.infra.JwtTokenProvider;
import com.example.stormpaws.infra.security.CustomUserDetails;
import com.example.stormpaws.infra.security.CustomUserDetailsService;
import com.example.stormpaws.infra.security.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private CustomUserDetailsService customUserDetailsService;

  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void testDoFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
    // 준비: Mock HttpServletRequest에 "Authorization" 헤더 설정
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);

    String token = "valid-token";
    String bearerHeader = "Bearer " + token;
    request.addHeader("Authorization", bearerHeader);

    // JWT 토큰이 유효함을 가정하고, 토큰에서 "user-id-1"을 추출하도록 설정
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn("user-id-1");

    // 테스트용 사용자(UserModel)를 생성하고 CustomUserDetails로 래핑
    UserModel user =
        UserModel.builder().id("user-id-1").name("Test User").email("test@example.com").build();
    CustomUserDetails userDetails = new CustomUserDetails(user);

    // CustomUserDetailsService가 user-id-1에 대해 userDetails를 반환하도록 설정
    when(customUserDetailsService.loadUserById("user-id-1")).thenReturn(userDetails);

    // Act: 필터 실행
    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    // Assert: SecurityContextHolder에 올바른 인증 정보가 설정되었는지 확인
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth, "Authentication should not be null");
    assertTrue(
        auth instanceof UsernamePasswordAuthenticationToken,
        "Authentication must be instance of UsernamePasswordAuthenticationToken");
    assertEquals(userDetails, auth.getPrincipal(), "Principal should match the CustomUserDetails");

    // 필터 체인의 다음 단계가 호출되었는지 검증
    verify(filterChain, times(1)).doFilter(request, response);
  }
}
