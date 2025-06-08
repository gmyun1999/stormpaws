package com.example.stormpaws.web.dto.request;

import com.example.stormpaws.domain.constant.WeatherType;
import jakarta.validation.constraints.NotNull;

public record BattleComputerPVPBody(
    @NotNull String attackerDeckId,
    @NotNull String attackerUserId,
    @NotNull WeatherType weatherType) {}
