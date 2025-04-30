package com.example.stormpaws.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class DeckCardModel {

  @Id
  @Column(length = 36)
  private String id;

  @ManyToOne
  @JoinColumn(name = "deck_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  private DeckModel deck;

  @ManyToOne
  @JoinColumn(
      name = "card_id",
      nullable = false,
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  private CardModel card;

  @Column(name = "card_pos", nullable = false)
  private int pos; // 덱 내 카드 순서

  @Column(name = "quantity", nullable = false)
  private int cardQuantity; // 카드 수량
}
