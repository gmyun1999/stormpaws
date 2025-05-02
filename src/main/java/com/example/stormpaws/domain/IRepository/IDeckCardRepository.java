package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.model.DeckCardModel;
import java.util.List;
import java.util.Optional;

public interface IDeckCardRepository {
  DeckCardModel save(DeckCardModel deckCard);

  <S extends DeckCardModel> List<S> saveAll(Iterable<S> entities);

  Optional<DeckCardModel> findById(String id);
}
