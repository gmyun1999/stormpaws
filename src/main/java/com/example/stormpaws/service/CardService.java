package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.ICardRepository;
import com.example.stormpaws.domain.model.CardModel;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CardService {

  private final ICardRepository cardRepository;

  public CardService(ICardRepository cardRepository) {
    this.cardRepository = cardRepository;
  }

  public List<CardModel> findCardsByIds(List<String> cardIds) {
    // 카드 모델들을 한 번에 가져옴
    List<CardModel> cards = cardRepository.findAllById(cardIds);

    // 카드 수와 요청한 카드 수가 다르면 일부 카드가 누락된 것
    if (cards.size() != cardIds.size()) {
      List<String> missingCardIds = new ArrayList<>();

      // 각 카드 ID가 반환된 카드 리스트에 있는지 확인
      for (String cardId : cardIds) {
        boolean found = false;
        for (CardModel card : cards) {
          if (card.getId().equals(cardId)) {
            found = true;
            break; // 찾으면 더 이상 검사할 필요 없음
          }
        }

        // 찾지 못한 카드 ID는 missingCardIds에 추가
        if (!found) {
          missingCardIds.add(cardId);
        }
      }

      // 누락된 카드가 있으면 예외를 던짐
      if (!missingCardIds.isEmpty()) {
        throw new IllegalArgumentException(
            "Card(s) not found: " + String.join(", ", missingCardIds));
      }
    }

    return cards; // 모든 카드가 유효하면 카드 리스트 반환
  }
}
