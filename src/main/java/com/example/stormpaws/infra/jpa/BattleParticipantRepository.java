package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IBattleParticipantRepository;
import com.example.stormpaws.domain.model.BattleParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleParticipantRepository
    extends JpaRepository<BattleParticipant, String>, IBattleParticipantRepository {}
