package com.example.stormpaws.service.dto;

import java.util.List;

public record BattleResultDTO(String winnerDeckId, List<BattleLogDTO> logs) {}
