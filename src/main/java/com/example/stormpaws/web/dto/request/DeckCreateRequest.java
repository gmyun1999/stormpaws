package com.example.stormpaws.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DeckCreateRequest(
    @NotNull @Size(min = 3, max = 50) String name, @NotNull List<CardDTO> cards) {
  public record CardDTO(@NotNull String cardId, @Min(1) int quantity, @Min(1) int pos) {}
}
