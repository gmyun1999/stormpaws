package com.example.stormpaws.service.dto;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.constant.WeatherType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
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
  @NotNull(message = "도시 정보는 필수입니다")
  private City city;

  @NotNull(message = "날씨 타입은 필수입니다")
  private WeatherType weatherType;

  @NotNull(message = "조회 시간은 필수입니다")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime fetchedAt;
}
