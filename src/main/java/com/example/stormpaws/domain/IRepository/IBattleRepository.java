package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.model.BattleModel;
import java.util.List;
import java.util.Optional;

public interface IBattleRepository {

  Optional<BattleModel> findById(String id);

  List<BattleModel> findAll();

  BattleModel save(BattleModel entity);

  List<BattleModel> findAllById(Iterable<String> ids);

  <S extends BattleModel> List<S> saveAll(Iterable<S> entities);
}
