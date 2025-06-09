package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.model.BattleParticipant;
import com.example.stormpaws.domain.model.UserModel;
import java.util.List;
import java.util.Optional;

public interface IBattleParticipantRepository {

  Optional<BattleParticipant> findById(String id);

  List<BattleParticipant> findByUser(UserModel user);

  List<BattleParticipant> findAll();

  BattleParticipant save(BattleParticipant entity);

  List<BattleParticipant> findAllById(Iterable<String> ids);

  <S extends BattleParticipant> List<S> saveAll(Iterable<S> entities);
}
