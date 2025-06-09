package com.example.stormpaws.web.controller;

import com.example.stormpaws.service.ComputerDeckService;
import com.example.stormpaws.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@PreAuthorize("isAuthenticated()")
public class AdminController {

  private final ComputerDeckService computerDeckService;
  private final WeatherService weatherService;

  public AdminController(ComputerDeckService computerDeckService, WeatherService weatherService) {
    this.computerDeckService = computerDeckService;
    this.weatherService = weatherService;
  }

  /** 컴퓨터 덱 30개 생성 */
  @PostMapping("/computer-decks/init")
  public ResponseEntity<String> initComputerDecks() {
    computerDeckService.registerMultipleComputerDecks("Computer Deck", 30);
    return ResponseEntity.ok("30개의 컴퓨터 덱이 생성되었습니다.");
  }

  /** 전체 도시 날씨 초기화 */
  @PostMapping("/weather/init")
  public ResponseEntity<String> initWeather() {
    weatherService.updateAllCitiesWeather();
    return ResponseEntity.ok("날씨 데이터가 초기화(저장)되었습니다.");
  }
}
