package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IDeckCardRepository;
import com.example.stormpaws.domain.model.DeckCardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DeckCardRepository
    extends JpaRepository<DeckCardModel, String>, IDeckCardRepository {
  @Modifying
  @Transactional
  @Query("DELETE FROM DeckCardModel dc WHERE dc.deck.id = :deckId")
  void deleteByDeckId(@Param("deckId") String deckId);
}
