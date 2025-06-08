package com.example.stormpaws.infra.OpenMeteo.responseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenMeteoCurrentWeather {

  @JsonProperty("temperature")
  private double temperature;

  @JsonProperty("windspeed")
  private double windSpeed;

  @JsonProperty("weathercode")
  private int weatherCode;

  @JsonProperty("time")
  private String time;

  public double getTemperature() {
    return temperature;
  }

  public double getWindSpeed() {
    return windSpeed;
  }

  public int getWeatherCode() {
    return weatherCode;
  }

  public String getTime() {
    return time;
  }
}
