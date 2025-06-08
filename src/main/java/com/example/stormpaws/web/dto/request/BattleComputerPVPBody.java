package com.example.stormpaws.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record BattleComputerPVPBody(
    @NotNull String attackerDeckId, @NotNull String attackerUserId, @NotNull String weatherLogId) {}
