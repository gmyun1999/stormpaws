package com.example.stormpaws.infra.security;

import com.example.stormpaws.domain.constant.UserRole;
import com.example.stormpaws.domain.model.UserModel;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

  private final UserModel user;

  public CustomUserDetails(UserModel user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(UserRole.ROLE_USER.name()));
  }

  @Override
  public String getPassword() {
    // JWT 기반 인증이여서 필요없
    return null;
  }

  @Override
  public String getUsername() {
    // 인증의 기본 키로 사용할 정보를 반환(여기서는 email 사용)
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public UserModel getUser() {
    return user;
  }
}
