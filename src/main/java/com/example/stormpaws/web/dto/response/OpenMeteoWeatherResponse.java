package com.example.stormpaws.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpenMeteoWeatherResponse {
  @JsonProperty("current_weather")
  private CurrentWeather current_weather;

  public CurrentWeather getCurrentWeather() {
    return current_weather;
  }

  @Data
  public static class CurrentWeather {
    private int weathercode;

    public int getWeathercode() {
      return weathercode;
    }
  }
}
