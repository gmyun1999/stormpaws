package com.example.stormpaws.domain.model;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.constant.WeatherType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
@Table(indexes = {@Index(name = "idx_city_fetched_at", columnList = "city, fetched_at")})
public class WeatherLogModel {

  @Id
  @Column(length = 36)
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private City city;

  @Enumerated(EnumType.STRING)
  @Column(name = "weather_type", nullable = false)
  private WeatherType weatherType;

  @Column(name = "fetched_at", nullable = false)
  private LocalDateTime fetchedAt;
}
