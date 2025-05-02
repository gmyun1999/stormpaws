package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.model.CardModel;
import java.util.List;
import java.util.Optional;

public interface ICardRepository {

  Optional<CardModel> findById(String cardId);

  List<CardModel> findAll();

  CardModel save(CardModel cardModel);

  List<CardModel> findAllById(Iterable<String> ids);

  // bulk save
  <S extends CardModel> List<S> saveAll(Iterable<S> entities);
}
