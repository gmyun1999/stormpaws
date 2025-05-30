package com.example.stormpaws.service.dto;

public record BattleLogDTO(
    double timestamp,
    String attackerCardId,
    String targetCardId,
    int damage,
    int targetRemainingHp) {}
