package com.example.stormpaws.infra.OpenMeteo.responseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenMeteoBulkWeatherResponse {

  @JsonProperty("latitude")
  private double latitude;

  @JsonProperty("longitude")
  private double longitude;

  @JsonProperty("location_id")
  private int locationId;

  @JsonProperty("current_weather")
  private OpenMeteoCurrentWeather currentWeather;

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public int getLocationId() {
    return locationId;
  }

  public OpenMeteoCurrentWeather getCurrentWeather() {
    return currentWeather;
  }
}
