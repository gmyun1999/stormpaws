package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.service.WeatherService;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
  private final WeatherService weatherService;

  // 도시 목록과 날씨 확률 조회
  @GetMapping("/cities")
  public ResponseEntity<Map<String, Object>> getCities() {
    Map<String, Object> result = weatherService.getCities();
    return ResponseEntity.ok(result);
  }

  // 날씨 데이터 조회
  @GetMapping("/{city}")
  public ResponseEntity<CityWeatherInfoDTO> getWeather(@PathVariable("city") City city) {
    CityWeatherInfoDTO weatherInfo = weatherService.fetchWeather(city);
    return ResponseEntity.ok(weatherInfo);
  }

  @GetMapping("/random")
  public ResponseEntity<CityWeatherInfoDTO> getRandomWeather() {
    City city = City.values()[ThreadLocalRandom.current().nextInt(City.values().length)];
    CityWeatherInfoDTO weatherInfo = weatherService.fetchWeather(city);
    return ResponseEntity.ok(weatherInfo);
  }
}
