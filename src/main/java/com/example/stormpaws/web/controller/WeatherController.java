package com.example.stormpaws.web.controller;

import com.example.stormpaws.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

  private final WeatherService weatherService;

  public WeatherController(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  @GetMapping("/fetch")
  public ResponseEntity<String> fetchWeather() {
    weatherService.updateAllCitiesWeather(); // 내부 로직은 그대로 사용
    return ResponseEntity.ok("Weather data fetched and saved");
  }
}
