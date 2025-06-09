package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.IBattleParticipantRepository;
import com.example.stormpaws.domain.IRepository.IBattleRepository;
import com.example.stormpaws.domain.IRepository.IDeckRepository;
import com.example.stormpaws.domain.IRepository.IUserRepository;
import com.example.stormpaws.domain.IRepository.IWeatherRepository;
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
import com.example.stormpaws.service.dto.BattleRecordResponseDTO;
import com.example.stormpaws.service.dto.BattleResultDTO;
import com.example.stormpaws.service.dto.PagedResultDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BattleService {

  private final IDeckRepository deckRepo;
  private final IBattleRepository battleRepo;
  private final IBattleParticipantRepository battleParticipantRepo;
  private final IUserRepository userRepo;
  private final IWeatherRepository weatherLogRepo;
  private final BattleSimulator simulator;
  private final ObjectMapper objectMapper;

  public BattleService(
      IDeckRepository deckRepo,
      IBattleRepository battleRepo,
      IBattleParticipantRepository battleParticipantRepo,
      IUserRepository userRepo,
      IWeatherRepository weatherLogRepo,
      BattleSimulator simulator,
      ObjectMapper objectMapper) {
    this.deckRepo = deckRepo;
    this.battleRepo = battleRepo;
    this.battleParticipantRepo = battleParticipantRepo;
    this.userRepo = userRepo;
    this.weatherLogRepo = weatherLogRepo;
    this.simulator = simulator;
    this.objectMapper = objectMapper;
  }

  /**
   * PVP 배틀을 수행하고 결과를 DB에 저장한 뒤 DTO를 반환한다.
   *
   * @param attackerUserId 공격자 유저 ID
   * @param attackerDeckId 공격자 덱 ID
   * @param defenderUserId 수비자 유저 ID
   * @param defenderDeckId 수비자 덱 ID
   * @param weatherLogId 날씨 로그 ID
   * @param battleType 배틀 타입
   * @return BattleResultDTO 시뮬레이션 결과 DTO
   */
  @Transactional
  public BattleResultDTO startPVPBattle(
      String attackerUserId,
      String attackerDeckId,
      String defenderUserId,
      String defenderDeckId,
      String weatherLogId,
      BattleType battleType) {
    WeatherLogModel weatherLog =
        weatherLogRepo
            .findById(weatherLogId)
            .orElseThrow(
                () -> new IllegalArgumentException("유효하지 않은 WeatherLog ID: " + weatherLogId));

    BattleResultDTO resultDTO =
        runSimulation(attackerDeckId, defenderDeckId, weatherLog.getWeatherType());

    Map<String, Object> eventLogData = new HashMap<>();
    eventLogData.put("attackerDeckId", attackerDeckId);
    eventLogData.put("defenderDeckId", defenderDeckId);
    eventLogData.put("weather", weatherLog.getWeatherType().name());
    eventLogData.put("winnerDeckId", resultDTO.winnerDeckId());
    eventLogData.put("logs", resultDTO.logs());

    String eventLogJson = toJson(eventLogData);

    LocalDateTime now = LocalDateTime.now();

    BattleModel battle = saveBattle(weatherLog, now, now, eventLogJson, battleType);

    List<BattleParticipant> participants =
        buildParticipants(
            battle,
            attackerUserId,
            attackerDeckId,
            defenderUserId,
            defenderDeckId,
            resultDTO.winnerDeckId());
    saveParticipants(participants);

    return resultDTO;
  }

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

  private String toJson(Map<String, Object> dto) {
    try {
      return objectMapper.writeValueAsString(dto);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("BattleResultDTO JSON 변환 실패", e);
    }
  }

  private BattleModel saveBattle(
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

  // battle에 쓰이는 배틀 전용 유닛 리스트 생성
  private List<Unit> toUnits(DeckModel deck, WeatherType weather) {
    var cards = new ArrayList<>(deck.getDecklist());
    cards.sort(Comparator.comparingInt(DeckCardModel::getPos));

    List<Unit> units = new ArrayList<>();
    for (DeckCardModel dc : cards) {
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

  public PagedResultDTO<BattleRecordResponseDTO> getMyRecordList(
      UserModel user, int page, int size) {
    List<BattleParticipant> myParticipants = battleParticipantRepo.findByUser(user);
    List<BattleRecordResponseDTO> result = new ArrayList<>();

    for (BattleParticipant myPart : myParticipants) {
      BattleModel battle = myPart.getBattle();
      BattleResult battleResult = myPart.getBattleResult();

      // battleEventLog에서 내 덱 id, 상대 덱 id, 날씨 정보 추출 (파싱 필요)
      Map<String, Object> log = parseBattleEventLog(battle.getBattleEventLog());
      String myDeckId = (String) log.get("attackerDeckId");
      String opponentDeckId = (String) log.get("defenderDeckId");
      WeatherType weather = WeatherType.valueOf((String) log.get("weather"));

      // 상대 덱 id로 DeckModel 조회 → 상대 userId 추출
      DeckModel opponentDeck = deckRepo.findByIdWithDeckCardsAndCards(opponentDeckId).orElse(null);
      String opponentUserId =
          (opponentDeck != null && opponentDeck.getUser() != null)
              ? opponentDeck.getUser().getId()
              : null;

      // 내 덱 정보 조회
      DeckModel myDeck = deckRepo.findByIdWithDeckCardsAndCards(myDeckId).orElse(null);

      // DTO 조합
      BattleRecordResponseDTO dto = new BattleRecordResponseDTO();
      dto.setResult(battleResult);
      dto.setOpponentUserId(opponentUserId);
      dto.setOpponentDeck(opponentDeck);
      dto.setMyDeck(myDeck);
      dto.setWeather(weather);

      result.add(dto);
    }
    return Paginator.paginate(result, page, size);
  }

  // battleEventLog 파싱 메서드는 실제 구조에 맞게 구현 필요
  private Map<String, Object> parseBattleEventLog(String eventLogJson) {
    try {
      return objectMapper.readValue(eventLogJson, new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException("BattleEventLog JSON 파싱 실패", e);
    }
  }
}
