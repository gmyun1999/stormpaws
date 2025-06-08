package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.constant.BattleType;
import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.domain.model.DeckModel;
import com.example.stormpaws.domain.model.UserModel;
import com.example.stormpaws.service.BattleService;
import com.example.stormpaws.service.DeckService;
import com.example.stormpaws.service.SystemUserService;
import com.example.stormpaws.service.dto.BattleResultDTO;
import com.example.stormpaws.web.dto.request.BattleComputerPVPBody;
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
  private final DeckService deckService;
  private final SystemUserService systemUserService;

  public BattleController(
      BattleService battleService, DeckService deckService, SystemUserService systemUserService) {
    this.battleService = battleService;
    this.deckService = deckService;
    this.systemUserService = systemUserService;
  }

  @PostMapping("/pvp")
  public ResponseEntity<ApiResponse<BattleResultDTO>> simulatePvp(
      @Valid @RequestBody BattlePVPBody requestDTO) {
    String attackerDeckId = requestDTO.attackerDeckId();
    String defenderDeckId = requestDTO.defenderDeckId();
    String attackerUserId = requestDTO.attackerUserId();
    String defenderUserId = requestDTO.defenderUserId();
    WeatherType weatherType = requestDTO.weatherType();

    BattleResultDTO result =
        battleService.startPVPBattle(
            attackerUserId,
            attackerDeckId,
            defenderUserId,
            defenderDeckId,
            weatherType,
            BattleType.asyncPvp1v1);

    ApiResponse<BattleResultDTO> response = new ApiResponse<>(true, "success", result);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/pvp/computer/random")
  public ResponseEntity<ApiResponse<BattleResultDTO>> simulateComputerPvp(
      @Valid @RequestBody BattleComputerPVPBody requestDTO) {
    String attackerDeckId = requestDTO.attackerDeckId();
    String attackerUserId = requestDTO.attackerUserId();
    WeatherType weatherType = requestDTO.weatherType();

    UserModel computerUser = systemUserService.getSystemUser();
    DeckModel computerDeck = deckService.getRandomDeckForUser(computerUser.getId());
    String defenderUserId = computerUser.getId();
    String defenderDeckId = computerDeck.getId();

    BattleResultDTO result =
        battleService.startPVPBattle(
            attackerUserId,
            attackerDeckId,
            defenderUserId,
            defenderDeckId,
            weatherType,
            BattleType.computerPvp1v1);

    ApiResponse<BattleResultDTO> response = new ApiResponse<>(true, "success", result);
    return ResponseEntity.ok(response);
  }
}
