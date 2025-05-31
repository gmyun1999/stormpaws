package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.service.BattleService;
import com.example.stormpaws.service.dto.BattleResultDTO;
import com.example.stormpaws.web.dto.request.BattlePVPBody;
import com.example.stormpaws.web.dto.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/battles")
public class BattleController {

  private final BattleService battleService;

  public BattleController(BattleService battleService) {
    this.battleService = battleService;
  }

  @PostMapping("/pvp")
  public ResponseEntity<ApiResponse<BattleResultDTO>> simulatePvp(
      @Valid @RequestBody BattlePVPBody requestDTO) {
    String attackerDeckId = requestDTO.attackerDeckId();
    String defenderDeckId = requestDTO.defenderDeckId();
    WeatherType weather = requestDTO.weather();
    BattleResultDTO result =
        this.battleService.runSimulation(attackerDeckId, defenderDeckId, weather);
    ApiResponse<BattleResultDTO> response = new ApiResponse<>(true, "success", result);
    return ResponseEntity.ok(response);
  }
}
