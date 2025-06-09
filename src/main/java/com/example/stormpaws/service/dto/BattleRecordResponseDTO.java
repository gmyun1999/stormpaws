package com.example.stormpaws.service.dto;

import com.example.stormpaws.domain.constant.BattleResult;
import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.domain.model.DeckModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BattleRecordResponseDTO {
  private BattleResult result; // WIN/LOSE
  private String opponentUserId;
  private DeckModel opponentDeck;
  private DeckModel myDeck;
  private WeatherType weather;
}
