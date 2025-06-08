package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IBattleRepository;
import com.example.stormpaws.domain.model.BattleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleRepository extends JpaRepository<BattleModel, String>, IBattleRepository {}
