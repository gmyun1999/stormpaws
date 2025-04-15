package com.example.stormpaws.infra.security;

import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // 여기서는 username을 email로 간주하여 처리함
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserModel user =
        userRepository
            .findByEmail(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + username));
    return new CustomUserDetails(user);
  }

  // JWT의 subject로 전달한 값(예: 사용자 id)로 사용자 조회 기능 추가
  public UserDetails loadUserById(String id) throws UsernameNotFoundException {
    UserModel user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    return new CustomUserDetails(user);
  }
}
