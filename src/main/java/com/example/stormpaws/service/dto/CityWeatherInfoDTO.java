package com.example.stormpaws.service.dto;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.constant.WeatherType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityWeatherInfoDTO {
  private City city;
  private WeatherType weatherType;
  private LocalDateTime fetchedAt;
}
