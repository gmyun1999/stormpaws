package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.ICardRepository;
import com.example.stormpaws.domain.model.CardModel;
import com.example.stormpaws.service.dto.PagedResultDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CardService {

  private final ICardRepository cardRepository;

  public CardService(ICardRepository cardRepository) {
    this.cardRepository = cardRepository;
  }


  public CardModel getCardById(String cardId) {
    Optional<CardModel> card = cardRepository.findById(cardId);
    return card.orElseThrow(
        () -> new IllegalArgumentException("Card not found with id: " + cardId));
  }

  public PagedResultDTO<CardModel> getCardList(int page, int size) {
    // 1. 저장소에서 모든 카드 목록을 가져옵니다.
    List<CardModel> allCards = cardRepository.findAll(); // ICardRepository의 findAll() 사용 [1][2]

    return Paginator.paginate(allCards, page, size);

  }

  public List<CardModel> findCardsByIds(List<String> cardIds) {

    List<CardModel> cards = cardRepository.findAllById(cardIds);

    if (cards.size() != cardIds.size()) {
      List<String> missingCardIds = new ArrayList<>();

      // 각 카드 ID가 반환된 카드 리스트에 있는지 확인
      for (String cardId : cardIds) {
        boolean found = false;
        for (CardModel card : cards) {
          if (card.getId().equals(cardId)) {
            found = true;
            break;
          }
        }

        if (!found) {
          missingCardIds.add(cardId);
        }
      }

      if (!missingCardIds.isEmpty()) {
        throw new IllegalArgumentException(
            "Card(s) not found: " + String.join(", ", missingCardIds));
      }
    }

    return cards;
  }
}