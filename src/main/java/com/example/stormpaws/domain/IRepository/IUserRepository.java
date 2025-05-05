package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.model.UserModel;
import java.util.List;
import java.util.Optional;

public interface IUserRepository {
  UserModel save(UserModel user);

  Optional<UserModel> findByOauthTypeAndOauthId(String oauthType, String oauthId);

  Optional<UserModel> findByEmail(String email);

  Optional<UserModel> findById(String id);

  List<String> findAllUserId();
}
