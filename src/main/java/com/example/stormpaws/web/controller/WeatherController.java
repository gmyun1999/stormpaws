package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.model.WeatherLogModel;
import com.example.stormpaws.service.WeatherService;
import com.example.stormpaws.web.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

  private final WeatherService weatherService;

  public WeatherController(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  @GetMapping("/random")
  public ResponseEntity<ApiResponse<WeatherLogModel>> getRandomCityWeather() {
    WeatherLogModel log = weatherService.getRandomCityLatestWeather();
    ApiResponse<WeatherLogModel> response = new ApiResponse<>(true, "success", log);
    return ResponseEntity.ok(response);
  }
}
