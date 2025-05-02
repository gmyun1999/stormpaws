package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IUserRepository;
import com.example.stormpaws.domain.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, String>, IUserRepository {
  // TODO : querydsl로 변경하기

  @Override
  @Query("SELECT u.id FROM UserModel u")
  List<String> findAllUserId();
}
