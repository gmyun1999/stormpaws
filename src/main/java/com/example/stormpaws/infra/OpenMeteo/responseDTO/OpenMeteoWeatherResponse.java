package com.example.stormpaws.infra.OpenMeteo.responseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OpenMeteoWeatherResponse {

  @JsonProperty("current_weather")
  private List<OpenMeteoCurrentWeather> currentWeather;

  public List<OpenMeteoCurrentWeather> getCurrentWeather() {
    return currentWeather;
  }
}
