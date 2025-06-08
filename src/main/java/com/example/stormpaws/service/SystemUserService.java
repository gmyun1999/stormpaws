package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.IUserRepository;
import com.example.stormpaws.domain.model.UserModel;

public class SystemUserService {
  private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000001";
  private final IUserRepository userRepository;

  public SystemUserService(IUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /** 컴퓨터(시스템) 유저를 항상 반환 */
  public UserModel getSystemUser() {
    return userRepository
        .findById(SYSTEM_USER_ID)
        .orElseThrow(() -> new IllegalStateException("시스템 유저가 없습니다."));
  }
}
