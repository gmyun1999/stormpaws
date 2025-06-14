package com.example.stormpaws.service;

import com.example.stormpaws.service.dto.BattleLogDTO;
import com.example.stormpaws.service.dto.BattleResultDTO;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.springframework.stereotype.Component;

@Component
public class BattleSimulator {

  public static class Unit {
    public final String cardId; // 카드 식별자
    public int hp; // 현재 체력
    public final int attack; // 공격력
    public final int attacksPerSecond; // 초당 공격 횟수
    public double nextAttackTime; // 다음 공격 시점 단위는 초임.

    public Unit(String cardId, int hp, int attack, int attacksPerSecond) {
      this.cardId = cardId;
      this.hp = hp;
      this.attack = attack;
      this.attacksPerSecond = attacksPerSecond;
      this.nextAttackTime = 0.0;
    }

    public boolean isAlive() {
      return this.hp > 0;
    }
  }

  /**
   * 이미 생성된 유닛 리스트와 양쪽 덱 ID를 받아 시뮬레이션 실행
   *
   * @param attackerDeckId
   * @param attackers
   * @param defenderDeckId
   * @param defenders
   * @return BattleResultDTO(승자 + 로그)
   */
  public BattleResultDTO simulate(
      String attackerDeckId, List<Unit> attackers, String defenderDeckId, List<Unit> defenders) {

    Queue<Unit> aQueue = new LinkedList<>(attackers);
    Queue<Unit> bQueue = new LinkedList<>(defenders);

    Unit a = aQueue.poll();
    Unit b = bQueue.poll();
    List<BattleLogDTO> logs = new ArrayList<>();
    double currentTime = 0.0;

    // 체인 배틀
    while (a != null && b != null) {
      a.nextAttackTime = currentTime + (1.0 / a.attacksPerSecond);
      b.nextAttackTime = currentTime + (1.0 / b.attacksPerSecond);

      while (a.isAlive() && b.isAlive()) {

        // 어느 유닛이 다음 공격인지 결정
        Unit attacker = (a.nextAttackTime <= b.nextAttackTime) ? a : b;
        Unit target = (attacker == a) ? b : a;

        currentTime = attacker.nextAttackTime;
        target.hp = Math.max(0, target.hp - attacker.attack);

        // 덱 ID 분기 처리
        String logAttackerDeckId = (attacker == a) ? attackerDeckId : defenderDeckId;
        String logTargetDeckId = (target == b) ? defenderDeckId : attackerDeckId;

        logs.add(
            new BattleLogDTO(
                currentTime,
                logAttackerDeckId,
                attacker.cardId,
                logTargetDeckId,
                target.cardId,
                attacker.attack,
                target.hp));

        double interval = 1.0 / attacker.attacksPerSecond;
        attacker.nextAttackTime += interval;
      }

      // 살아남은 유닛 유지, 패자는 교체
      if (a.isAlive()) {
        b = bQueue.poll();
        if (b != null) {
          b.nextAttackTime = currentTime + (1.0 / b.attacksPerSecond);
        }
      } else {
        a = aQueue.poll();
        if (a != null) {
          a.nextAttackTime = currentTime + (1.0 / a.attacksPerSecond);
        }
      }
    }

    // 승자 결정
    String winner = (a != null && a.isAlive()) ? attackerDeckId : defenderDeckId;
    return new BattleResultDTO(winner, logs);
  }
}
