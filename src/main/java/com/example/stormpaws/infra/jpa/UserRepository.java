package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IUserRepository;
import com.example.stormpaws.domain.model.UserModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, String>, IUserRepository {
  Optional<UserModel> findByOauthTypeAndOauthId(String oauthType, String oauthId);

  Optional<UserModel> findByEmail(String email);

  Optional<UserModel> findById(String id);
}
