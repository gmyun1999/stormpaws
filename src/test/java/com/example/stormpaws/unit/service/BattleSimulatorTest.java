package com.example.stormpaws.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.stormpaws.service.BattleSimulator;
import com.example.stormpaws.service.BattleSimulator.Unit;
import com.example.stormpaws.service.dto.BattleLogDTO;
import com.example.stormpaws.service.dto.BattleResultDTO;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BattleSimulatorTest {

  @Test
  @DisplayName("하나의 공격자와 하나의 방어자가 있을 때, 공격자 우선 공격 속도가 같을 경우 방어자가 먼저 피격되는지 확인")
  void testSimulate_singleVsSingle_attackerFasterOrEqual() {
    // given
    Unit attacker = new Unit("cardA", 10, 5, 1);
    Unit defender = new Unit("cardB", 10, 3, 1);
    List<Unit> attackers = List.of(attacker);
    List<Unit> defenders = List.of(defender);
    BattleSimulator simulator = new BattleSimulator();

    // when
    BattleResultDTO result = simulator.simulate(attackers, defenders);

    // then
    assertEquals("attacker", result.winner(), "최종 승자는 공격자여야 한다");

    List<BattleLogDTO> logs = result.logs();
    assertEquals(2, logs.size(), "로그는 두 번 기록되어야 한다");

    BattleLogDTO firstLog = logs.get(0);
    assertEquals("cardA", firstLog.attackerCardId());
    assertEquals("cardB", firstLog.targetCardId());
    assertEquals(5, firstLog.damage());
    assertEquals(5, firstLog.targetRemainingHp());

    BattleLogDTO secondLog = logs.get(1);
    assertEquals("cardA", secondLog.attackerCardId());
    assertEquals("cardB", secondLog.targetCardId());
    assertEquals(5, secondLog.damage());
    assertEquals(0, secondLog.targetRemainingHp());
  }

  @Test
  @DisplayName("공격속도가 높은 유닛이 먼저 공격하여 승리하는지 확인")
  void testSimulate_speedPriority() {
    // given
    Unit attacker = new Unit("fastA", 6, 2, 2);
    Unit defender = new Unit("slowB", 6, 2, 1);
    List<Unit> attackers = List.of(attacker);
    List<Unit> defenders = List.of(defender);
    BattleSimulator simulator = new BattleSimulator();

    // when
    BattleResultDTO result = simulator.simulate(attackers, defenders);

    // then
    assertEquals("attacker", result.winner());
    assertTrue(
        result.logs().stream().allMatch(log -> log.attackerCardId().equals("fastA")),
        "모든 공격은 공격자가 수행해야 한다");
  }

  @Test
  @DisplayName("공격자 5명 vs 방어자 5명 일괄 전투 후 공격자가 승리하는지 확인")
  void testSimulate_fiveVsFive_attackerWins() {
    // given: 공격자 5명 (HP=15, 공격력=4, 속도=1)
    List<Unit> attackers =
        List.of(
            new Unit("cardA1", 15, 4, 1),
            new Unit("cardA2", 15, 4, 1),
            new Unit("cardA3", 15, 4, 1),
            new Unit("cardA4", 15, 4, 1),
            new Unit("cardA5", 15, 4, 1));
    // 방어자 5명 (HP=12, 공격력=3, 속도=1)
    List<Unit> defenders =
        List.of(
            new Unit("cardB1", 12, 3, 1),
            new Unit("cardB2", 12, 3, 1),
            new Unit("cardB3", 12, 3, 1),
            new Unit("cardB4", 12, 3, 1),
            new Unit("cardB5", 12, 3, 1));
    BattleSimulator simulator = new BattleSimulator();

    // when
    BattleResultDTO result = simulator.simulate(attackers, defenders);

    // then: 공격자가 더 높은 공격력으로 유리하므로 승리 예상
    assertEquals("attacker", result.winner(), "공격자 5명이 승리해야 한다");
    // 로그가 빈 리스트가 아니어야 함
    assertFalse(result.logs().isEmpty(), "로그가 최소 한 건 이상 생성되어야 한다");
  }

  @Test
  @DisplayName("5대5 전투에서 단 1개의 유닛만 성능이 다르고, 그 차이로 승패가 결정되는지 확인")
  void testSimulate_fiveVsFive_oneUnitDifference() {
    // given: 공격자 5명 중 4명은 HP=10, 공격력=3, 속도=1, 하나만 속도=2로 빠르게 설정
    List<Unit> attackers =
        List.of(
            new Unit("cardA1", 10, 3, 2), // 속도가 더 빠른 유닛
            new Unit("cardA2", 10, 3, 1),
            new Unit("cardA3", 10, 3, 1),
            new Unit("cardA4", 10, 3, 1),
            new Unit("cardA5", 10, 3, 1));
    // 방어자 5명은 모두 HP=10, 공격력=3, 속도=1로 동일
    List<Unit> defenders =
        List.of(
            new Unit("cardB1", 10, 3, 1),
            new Unit("cardB2", 10, 3, 1),
            new Unit("cardB3", 10, 3, 1),
            new Unit("cardB4", 10, 3, 1),
            new Unit("cardB5", 10, 3, 1));
    BattleSimulator simulator = new BattleSimulator();

    // when
    BattleResultDTO result = simulator.simulate(attackers, defenders);

    // then: 공격자 중 한 명만 빠르므로 공격자 승리 예상
    assertEquals("attacker", result.winner(), "속도가 더 빠른 공격자가 승리해야 한다");

    // 속도 차이가 작으므로, 첫 교전에 속도 높은 카드가 먼저 공격하여 우세함을 확인
    BattleLogDTO firstLog = result.logs().get(0);
    assertEquals("cardA1", firstLog.attackerCardId(), "첫 공격은 속도 빠른 cardA1 이어야 한다");
    assertEquals("cardB1", firstLog.targetCardId(), "첫 교전 상대는 방어자의 첫 유닛이어야 한다");
  }

  @Test
  @DisplayName("5대5 전투에서 처음 네 유닛은 동일하고 마지막 한 유닛만 차이로 승패가 결정되는지 확인")
  void testSimulate_fiveVsFive_lastUnitDifference() {
    // given: 공격자 5명 중 처음 네 명은 HP=10, 공격력=3, 속도=1로 동일,
    //       마지막 한 명만 공격속도가 2로 더 빠르게 설정
    List<Unit> attackers =
        List.of(
            new Unit("cardA1", 10, 3, 1),
            new Unit("cardA2", 10, 3, 1),
            new Unit("cardA3", 10, 3, 1),
            new Unit("cardA4", 10, 3, 1),
            new Unit("cardA5", 10, 3, 2) // 마지막 유닛만 속도 2
            );
    // 방어자 5명은 모두 HP=10, 공격력=3, 속도=1로 동일
    List<Unit> defenders =
        List.of(
            new Unit("cardB1", 10, 3, 1),
            new Unit("cardB2", 10, 3, 1),
            new Unit("cardB3", 10, 3, 1),
            new Unit("cardB4", 10, 3, 1),
            new Unit("cardB5", 10, 3, 1));
    BattleSimulator simulator = new BattleSimulator();

    // when
    BattleResultDTO result = simulator.simulate(attackers, defenders);

    // then: 마지막 유닛 차이가 공격자의 승리를 결정해야 한다
    assertEquals("attacker", result.winner(), "마지막 유닛 차이로 공격자가 승리해야 한다");

    // 마지막 교전에서 cardA5가 cardB5를 공격했음을 확인
    boolean lastUnitParticipated =
        result.logs().stream()
            .anyMatch(
                log ->
                    log.attackerCardId().equals("cardA5") && log.targetCardId().equals("cardB5"));
    assertTrue(lastUnitParticipated, "마지막 교전에 cardA5가 cardB5를 공격했어야 한다");
  }
}
