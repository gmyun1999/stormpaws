package com.example.stormpaws.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.stormpaws.domain.IRepository.IDeckRepository;
import com.example.stormpaws.domain.model.CardModel;
import com.example.stormpaws.domain.model.DeckCardModel;
import com.example.stormpaws.domain.model.DeckModel;
import com.example.stormpaws.service.BattleService;
import com.example.stormpaws.service.BattleSimulator;
import com.example.stormpaws.service.BattleSimulator.Unit;
import com.example.stormpaws.service.dto.BattleResultDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

  @Mock private IDeckRepository deckRepo;

  @Mock private BattleSimulator simulator;

  @InjectMocks private BattleService battleService;

  // @Captor 애노테이션으로 제네릭 타입을 선언하면 unchecked 경고가 사라집니다.
  @Captor private ArgumentCaptor<List<Unit>> captorAtt;

  @Captor private ArgumentCaptor<List<Unit>> captorDef;

  @Test
  @DisplayName("정상적인 덱 ID가 주어졌을 때, toUnits로 변환 후 simulate 호출 확인")
  void testRunSimulation_callsSimulatorWithCorrectUnits() {
    // given: 더미 CardModel 생성
    CardModel cardA =
        CardModel.builder()
            .id("cardA")
            .name("A")
            .attackPower(5)
            .attackSpeed(1)
            .health(10)
            .cardType("SUNNY")
            .additionalCoefficient(2)
            .build();

    DeckCardModel deckCardA =
        DeckCardModel.builder().id("dcA").card(cardA).pos(1).cardQuantity(2).build();

    DeckModel dummyDeck =
        DeckModel.builder()
            .id("deck1")
            .name("DummyDeck")
            .user(null)
            .decklist(List.of(deckCardA))
            .isDefaultDeck(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    when(deckRepo.findByIdWithDeckCardsAndCards("deck123")).thenReturn(Optional.of(dummyDeck));
    when(deckRepo.findByIdWithDeckCardsAndCards("deck456")).thenReturn(Optional.of(dummyDeck));

    BattleResultDTO fakeResult = new BattleResultDTO("attacker", List.of());
    when(simulator.simulate(anyList(), anyList())).thenReturn(fakeResult);

    // when
    BattleResultDTO result =
        battleService.runSimulation(
            "deck123", "deck456", com.example.stormpaws.domain.constant.WeatherType.CLEAR);

    // then: @Captor로 받은 ArgumentCaptor<List<Unit>>를 그대로 사용
    verify(simulator).simulate(captorAtt.capture(), captorDef.capture());

    List<Unit> attackerUnits = captorAtt.getValue();
    List<Unit> defenderUnits = captorDef.getValue();

    // CLEAR 날씨와 일치하므로 effectiveAttack = 5 * 2 = 10
    assertEquals(2, attackerUnits.size(), "공격자 유닛 개수는 2개여야 한다");
    assertEquals(10, attackerUnits.get(0).attack, "첫 번째 유닛의 공격력은 10이어야 한다");
    assertEquals(10, attackerUnits.get(1).attack, "두 번째 유닛의 공격력도 10이어야 한다");

    assertEquals(2, defenderUnits.size(), "방어자 유닛 개수도 2개여야 한다");
    assertEquals("attacker", result.winner(), "반환된 BattleResultDTO의 승자는 'attacker'여야 한다");
  }
}
