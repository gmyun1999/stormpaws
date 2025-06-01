package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.service.WeatherService;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
  private final WeatherService weatherService;
  private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

  // 도시 목록과 날씨 확률 조회
  @GetMapping("/cities")
  public ResponseEntity<Map<String, Object>> getCities() {
    Map<String, Object> result = weatherService.getCities();
    if (result != null
        && result.containsKey("cityWeathers")
        && result.get("cityWeathers") instanceof Map) {
      log.info(
          "Returning weather summary with {} cities",
          ((Map<?, ?>) result.get("cityWeathers")).size());
    }
    return ResponseEntity.ok(result);
  }

  // 날씨 데이터 조회
  @GetMapping("/{city}")
  public ResponseEntity<CityWeatherInfoDTO> getWeather(@PathVariable("city") City city) {
    CityWeatherInfoDTO weatherInfo = weatherService.fetchWeather(city);
    weatherService.saveWeather(weatherInfo);
    return ResponseEntity.ok(weatherInfo);
  }
}
