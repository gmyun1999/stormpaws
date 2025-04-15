package com.example.stormpaws.infra.security;

import com.example.stormpaws.infra.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  public JwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = resolveToken(request);
    if (token != null && jwtTokenProvider.validateToken(token)) {
      String userId = jwtTokenProvider.getUserIdFromToken(token);

      // userId를 통해 사용자 정보를 조회
      var userDetails = customUserDetailsService.loadUserById(userId);
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
