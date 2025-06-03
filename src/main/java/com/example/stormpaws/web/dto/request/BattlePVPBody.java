package com.example.stormpaws.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record BattlePVPBody(
    @NotNull String attackerDeckId,
    @NotNull String attackerUserId,
    @NotNull String defenderUserId,
    @NotNull String defenderDeckId,
    @NotNull String weatherLogId) {}
