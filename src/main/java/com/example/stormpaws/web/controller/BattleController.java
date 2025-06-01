package com.example.stormpaws.web.controller;

import com.example.stormpaws.service.BattleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/battles")
public class BattleController {

  private final BattleService battleService;

  public BattleController(BattleService battleService) {
    this.battleService = battleService;
  }

  // @PostMapping("/pvp")
  // public ResponseEntity<ApiResponse<BattleResultDTO>> simulatePvp(
  //     @Valid @RequestBody BattlePVPBody requestDTO
  //   ) {
  //   String attackerDeckId = requestDTO.attackerDeckId();
  //   String defenderDeckId = requestDTO.defenderDeckId();

  //   WeatherType weather = requestDTO.weather();

  //   return ResponseEntity.ok(response);
  // }
}
