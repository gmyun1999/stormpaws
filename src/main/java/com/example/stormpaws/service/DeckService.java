package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.IDeckCardRepository;
import com.example.stormpaws.domain.IRepository.IDeckRepository;
import com.example.stormpaws.domain.model.CardModel;
import com.example.stormpaws.domain.model.DeckCardModel;
import com.example.stormpaws.domain.model.DeckModel;
import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.service.dto.DeckCardDTO;
import com.example.stormpaws.service.dto.PagedResultDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeckService {

  private final IDeckRepository deckRepository;
  private final IDeckCardRepository deckCardRepository;
  private final CardService cardService;

  public DeckService(
      IDeckRepository deckRepository,
      IDeckCardRepository deckCardRepository,
      CardService cardService) {
    this.deckRepository = deckRepository;
    this.deckCardRepository = deckCardRepository;
    this.cardService = cardService;
  }

  public PagedResultDTO<DeckModel> getMyDeckList(String userId, int page, int size) {
    List<DeckModel> allDecks = deckRepository.findByUserIdWithDeckCardsAndCards(userId);

    return Paginator.paginate(allDecks, page, size);
  }

  public DeckModel getDeckById(String deckId) {
    Optional<DeckModel> deck = deckRepository.findByIdWithDeckCardsAndCards(deckId);

    return deck.orElseThrow(
        () -> new IllegalArgumentException("Deck not found with id: " + deckId));
  }

  public DeckModel createDeck(DeckCardDTO deckCardDTO, UserModel user) {

    List<String> cardIds = new ArrayList<>();
    for (DeckCardDTO.CardDTO cardDTO : deckCardDTO.cards()) {
      cardIds.add(cardDTO.cardId());
    }

    List<CardModel> cards = cardService.findCardsByIds(cardIds);

    DeckModel deckModel =
        DeckModel.builder()
            .id(UUID.randomUUID().toString())
            .name(deckCardDTO.name())
            .user(user)
            .isDefaultDeck(false)
            .build();

    List<DeckCardModel> deckCardModels = new ArrayList<>();

    for (DeckCardDTO.CardDTO cardDTO : deckCardDTO.cards()) {
      CardModel card = null;

      for (CardModel c : cards) {
        if (c.getId().equals(cardDTO.cardId())) {
          card = c;
          break;
        }
      }

      if (card == null) {
        throw new IllegalArgumentException("Card not found: " + cardDTO.cardId());
      }

      DeckCardModel deckCardModel =
          DeckCardModel.builder()
              .id(UUID.randomUUID().toString())
              .deck(deckModel)
              .card(card)
              .pos(cardDTO.pos())
              .cardQuantity(cardDTO.quantity())
              .build();

      deckCardModels.add(deckCardModel);
    }
    saveDeckAndDeckCards(deckModel, deckCardModels);

    return deckModel;
  }

  public PagedResultDTO<DeckModel> getRandomDecks(int count, UserModel user) {
    // 1) 덱 보유 유저 ID 조회 + 현 유저 제외 + 랜덤 n명 선택
    List<String> userIds = deckRepository.findAllUserIdsWithDecks();
    userIds.remove(user.getId());
    if (userIds.size() > count) {
      Collections.shuffle(userIds);
      userIds = userIds.subList(0, count);
    }

    // 2) 선택된 유저들로부터 덱 랜덤 1개씩 뽑기
    List<DeckModel> randomDecks = new ArrayList<>(userIds.size());
    ThreadLocalRandom rnd = ThreadLocalRandom.current();
    for (String uid : userIds) {
      List<DeckModel> decks = deckRepository.findByUserIdWithDeckCardsAndCards(uid);
      DeckModel pick = decks.get(rnd.nextInt(decks.size()));
      randomDecks.add(pick);
    }
    int pageSize;
    if (randomDecks.isEmpty()) {
      pageSize = 1;
    } else {
      pageSize = Math.min(count, randomDecks.size());
    }
    return Paginator.paginate(randomDecks, 1, pageSize);
  }

  @Transactional
  private void saveDeckAndDeckCards(DeckModel deckModel, List<DeckCardModel> deckCardModels) {
    deckRepository.save(deckModel);
    deckCardRepository.saveAll(deckCardModels);
  }
}
