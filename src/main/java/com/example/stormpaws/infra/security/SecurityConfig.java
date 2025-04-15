package com.example.stormpaws.infra.security;

import com.example.stormpaws.infra.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final CustomUserDetailsService customUserDetailsService;
  private final JwtTokenProvider jwtTokenProvider;

  public SecurityConfig(
      CustomUserDetailsService customUserDetailsService, JwtTokenProvider jwtTokenProvider) {
    this.customUserDetailsService = customUserDetailsService;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Bean // Configuration이 붙은 클래스에서 @Bean을 사용하여 메서드를 정의하면, return 을 bean에 등록
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
    builder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    return builder.build();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authz -> authz.requestMatchers("/public/**").permitAll().anyRequest().authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
