package com.example.stormpaws.domain.model;

import com.example.stormpaws.domain.constant.BattleType;
import com.example.stormpaws.domain.constant.City;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
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
public class BattleModel {
  @Id
  @Column(length = 36)
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(name = "battle_type", length = 36)
  private BattleType battleType;

  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  @Column(name = "end_at", nullable = false)
  private LocalDateTime endAt;

  @ManyToOne
  @JoinColumn(
      name = "weather_log_id",
      nullable = false,
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  private WeatherLogModel weatherLog;

  @Enumerated(EnumType.STRING)
  @Column(name = "city", nullable = false)
  private City city;

  @Column(name = "battle_event_log", columnDefinition = "json", nullable = false)
  private String battleEventLog;
}
