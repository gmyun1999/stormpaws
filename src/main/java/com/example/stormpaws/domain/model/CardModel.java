package com.example.stormpaws.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class CardModel {
  @Id
  @Column(length = 36)
  private String id;

  @Column(length = 36, nullable = false)
  private String name;

  @Column(name = "공격력", nullable = false)
  private int attackPower;

  @Column(name = "공격속도", nullable = false)
  private int attackSpeed;

  @Column(name = "체력", nullable = false)
  private int health;

  @Column(name = "카드타입", nullable = false)
  private String cardType;

  @Column(name = "추가계수", nullable = false)
  private int additionalCoefficient;
}
