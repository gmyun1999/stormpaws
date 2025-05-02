package com.example.stormpaws.service.dto;

import java.util.List;

public record DeckCardDTO(String name, List<CardDTO> cards) {
  public record CardDTO(String cardId, int quantity, int pos) {}
}
