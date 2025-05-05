package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IDeckRepository;
import com.example.stormpaws.domain.model.DeckModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends JpaRepository<DeckModel, String>, IDeckRepository {
  // TODO: 나중에 이거 query dsl로 바꿔야함
  // TODO: 현재는 단일 메소드 매핑인데, Filter DTO에 따라서 fetch 가능하게 만들기.

  @Query(
      """
    SELECT d
    FROM DeckModel d
    LEFT JOIN FETCH d.decklist dc
    LEFT JOIN FETCH dc.card
    WHERE d.id = :deckId
    """)
  Optional<DeckModel> findByIdWithDeckCardsAndCards(@Param("deckId") String deckId);

  @Query(
      """
        SELECT DISTINCT d
        FROM DeckModel d
        LEFT JOIN FETCH d.decklist dc
        LEFT JOIN FETCH dc.card
        WHERE d.user.id = :userId
        """)
  List<DeckModel> findByUserIdWithDeckCardsAndCards(@Param("userId") String userId);

  @Query("SELECT DISTINCT d.user.id FROM DeckModel d")
  List<String> findAllUserIdsWithDecks();
}
