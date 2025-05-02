package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.model.DeckModel;
import java.util.List;
import java.util.Optional;

public interface IDeckRepository {

  // 덱 id에 해당하는 덱을 찾는 메소드
  Optional<DeckModel> findByIdWithDeckCardsAndCards(String deckId);

  // userId에 해당되는 모든 덱을 찾는 메소드
  List<DeckModel> findByUserIdWithDeckCardsAndCards(String userId);

  // 덱을 저장하는 메소드
  DeckModel save(DeckModel deckModel);
}
