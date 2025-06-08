package com.example.stormpaws.service;

import com.example.stormpaws.domain.model.DeckModel;
import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.service.dto.DeckCardDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComputerDeckService {

  private static final int DEFAULT_DECK_SIZE = 5;
  private static final int DEFAULT_CARD_QUANTITY = 5;

  private final CardService cardService;
  private final DeckService deckService;
  private final SystemUserService systemUserService;

  /** 단일 덱 템플릿 생성 */
  public DeckCardDTO createTemplate(String deckName) {
    var all = cardService.getAllCards();
    Collections.shuffle(all);

    var cards =
        IntStream.range(0, Math.min(DEFAULT_DECK_SIZE, all.size()))
            .mapToObj(i -> new DeckCardDTO.CardDTO(all.get(i).getId(), DEFAULT_CARD_QUANTITY, i))
            .toList();

    return new DeckCardDTO(deckName, cards);
  }

  /** 시스템 유저로 컴퓨터 덱 하나 생성·저장 */
  @Transactional
  public DeckModel registerComputerDeck(String deckName) {
    UserModel sysUser = systemUserService.getSystemUser();
    DeckCardDTO template = createTemplate(deckName);
    return deckService.createDeck(template, sysUser);
  }

  /**
   * 시스템 유저로 n개의 컴퓨터 덱을 생성·저장
   *
   * @param baseName 덱 이름 접두사 (예: "Computer Deck")
   * @param count 생성할 덱 개수
   */
  @Transactional
  public List<DeckModel> registerMultipleComputerDecks(String baseName, int count) {
    UserModel sysUser = systemUserService.getSystemUser();
    List<DeckModel> decks = new ArrayList<>(count);

    for (int i = 1; i <= count; i++) {
      String name = String.format("%s %02d", baseName, i);
      DeckCardDTO tpl = createTemplate(name);
      decks.add(deckService.createDeck(tpl, sysUser));
    }

    return decks;
  }
}
