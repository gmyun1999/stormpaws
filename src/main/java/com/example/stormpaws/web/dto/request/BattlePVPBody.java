package com.example.stormpaws.web.dto.request;

import com.example.stormpaws.domain.constant.WeatherType;
import jakarta.validation.constraints.NotNull;

public record BattlePVPBody(
    @NotNull String attackerDeckId, @NotNull String defenderDeckId, @NotNull WeatherType weather) {}
