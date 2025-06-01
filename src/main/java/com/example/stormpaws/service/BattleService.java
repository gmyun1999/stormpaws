package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.IBattleParticipantRepository;
import com.example.stormpaws.domain.IRepository.IBattleRepository;
import com.example.stormpaws.domain.IRepository.IDeckRepository;
import com.example.stormpaws.domain.IRepository.IUserRepository;
import com.example.stormpaws.domain.constant.BattleResult;
import com.example.stormpaws.domain.constant.BattleType;
import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.domain.model.BattleModel;
import com.example.stormpaws.domain.model.BattleParticipant;
import com.example.stormpaws.domain.model.CardModel;
import com.example.stormpaws.domain.model.DeckCardModel;
import com.example.stormpaws.domain.model.DeckModel;
import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.domain.model.WeatherLogModel;
import com.example.stormpaws.service.BattleSimulator.Unit;
import com.example.stormpaws.service.dto.BattleResultDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BattleService {

  private final IDeckRepository deckRepo;
  private final BattleSimulator simulator;
  private final IBattleRepository battleRepo;
  private final IBattleParticipantRepository battleParticipantRepo;
  private final ObjectMapper objectMapper;
  private final IUserRepository userRepo;

  public BattleService(
      IDeckRepository deckRepo,
      BattleSimulator simulator,
      IBattleRepository battleRepo,
      IBattleParticipantRepository battleParticipantRepo,
      ObjectMapper objectMapper,
      IUserRepository userRepo) {
    this.deckRepo = deckRepo;
    this.simulator = simulator;
    this.battleRepo = battleRepo;
    this.battleParticipantRepo = battleParticipantRepo;
    this.objectMapper = objectMapper;
    this.userRepo = userRepo;
  }

  /**
   * PVP 배틀을 수행하고 결과를 DB에 저장한 뒤 DTO를 반환한다.
   *
   * @param attackerUserId 공격자 유저 ID
   * @param attackerDeckId 공격자 덱 ID
   * @param defenderUserId 수비자 유저 ID
   * @param defenderDeckId 수비자 덱 ID
   * @param weatherLogId 날씨 로그 ID
   * @return BattleResultDTO 시뮬레이션 결과 DTO
   */
  // @Transactional
  // public BattleResultDTO startPVPBattle(
  //         String attackerUserId,
  //         String attackerDeckId,
  //         String defenderUserId,
  //         String defenderDeckId,
  //         String weatherLogId,
  //         BattleType battleType
  // ) {

  // WeatherLogModel weatherLog = weatherLogRepo.findById(weatherLogId)
  //         .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 WeatherLog ID: " +
  // weatherLogId));

  // BattleResultDTO resultDTO = runSimulation(
  //         attackerDeckId,
  //         defenderDeckId,
  //         weatherLog.getWeatherType()
  // );

  // LocalDateTime startAt = LocalDateTime.now();
  // LocalDateTime endAt = LocalDateTime.now();
  // String eventLogJson = toJson(resultDTO);

  // BattleModel battle = SaveBattle(
  //   weatherLog,
  //   startAt,
  //   endAt,
  //   eventLogJson,
  //   BattleType.asyncPvp1v1
  // );

  // saveParticipants(buildParticipants(
  //     battle,
  //     attackerUserId, attackerDeckId,
  //     defenderUserId, defenderDeckId,
  //     resultDTO.winnerDeckId()
  // ));

  // return resultDTO;
  // }

  private List<BattleParticipant> buildParticipants(
      BattleModel battle,
      String attackerUserId,
      String attackerDeckId,
      String defenderUserId,
      String defenderDeckId,
      String winnerDeckId) {
    UserModel attackerUser =
        userRepo
            .findById(attackerUserId)
            .orElseThrow(
                () -> new IllegalArgumentException("유효하지 않은 공격자 User ID: " + attackerUserId));
    UserModel defenderUser =
        userRepo
            .findById(defenderUserId)
            .orElseThrow(
                () -> new IllegalArgumentException("유효하지 않은 수비자 User ID: " + defenderUserId));

    BattleResult attackerResult =
        winnerDeckId.equals(attackerDeckId) ? BattleResult.WIN : BattleResult.LOSE;
    BattleResult defenderResult =
        winnerDeckId.equals(defenderDeckId) ? BattleResult.WIN : BattleResult.LOSE;

    BattleParticipant attacker =
        BattleParticipant.builder()
            .id(UUID.randomUUID().toString())
            .user(attackerUser)
            .battle(battle)
            .battleResult(attackerResult)
            .build();

    BattleParticipant defender =
        BattleParticipant.builder()
            .id(UUID.randomUUID().toString())
            .user(defenderUser)
            .battle(battle)
            .battleResult(defenderResult)
            .build();

    return List.of(attacker, defender);
  }

  private String toJson(BattleResultDTO dto) {
    try {
      return objectMapper.writeValueAsString(dto);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("BattleResultDTO JSON 변환 실패", e);
    }
  }

  private BattleModel SaveBattle(
      WeatherLogModel weatherLog,
      LocalDateTime startAt,
      LocalDateTime endAt,
      String eventLogJson,
      BattleType battleType) {
    BattleModel battle =
        BattleModel.builder()
            .id(UUID.randomUUID().toString())
            .battleType(battleType)
            .startAt(startAt)
            .endAt(endAt)
            .weatherLog(weatherLog)
            .battleEventLog(eventLogJson)
            .build();
    return battleRepo.save(battle);
  }

  private void saveParticipants(List<BattleParticipant> participants) {

    for (BattleParticipant p : participants) {
      battleParticipantRepo.save(p);
    }
  }

  private BattleResultDTO runSimulation(
      String attackerDeckId, String defenderDeckId, WeatherType weather) {
    DeckModel aDeck =
        deckRepo
            .findByIdWithDeckCardsAndCards(attackerDeckId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid deck ID: " + attackerDeckId));

    DeckModel bDeck =
        deckRepo
            .findByIdWithDeckCardsAndCards(defenderDeckId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid deck ID: " + defenderDeckId));

    List<Unit> attackers = toUnits(aDeck, weather);
    List<Unit> defenders = toUnits(bDeck, weather);

    return simulator.simulate(attackerDeckId, attackers, defenderDeckId, defenders);
  }

  private List<Unit> toUnits(DeckModel deck, WeatherType weather) {
    var cards = new ArrayList<>(deck.getDecklist());
    cards.sort(Comparator.comparingInt(DeckCardModel::getPos));

    List<Unit> units = new ArrayList<>();

    for (var dc : cards) {
      CardModel card = dc.getCard();
      int baseAttack = card.getAttackPower();
      boolean match = card.getCardType().equalsIgnoreCase(weather.name());
      int effectiveAttack = match ? baseAttack * card.getAdditionalCoefficient() : baseAttack;

      for (int i = 0; i < dc.getCardQuantity(); i++) {
        units.add(new Unit(card.getId(), card.getHealth(), effectiveAttack, card.getAttackSpeed()));
      }
    }

    return units;
  }
}
