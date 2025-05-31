package com.example.stormpaws.service.dto;

public record BattleLogDTO(
    double timestamp,
    String attackerDeckId,
    String attackerCardId,
    String targetDeckId,
    String targetCardId,
    int damage,
    int targetRemainingHp) {}
