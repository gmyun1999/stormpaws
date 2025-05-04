package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.model.CardModel;
import com.example.stormpaws.service.CardService;
import com.example.stormpaws.web.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
public class CardController {

  private final CardService cardService;

  public CardController(CardService cardService) {
    this.cardService = cardService;
  }

  // @GetMapping
  // public ResponseEntity<?> getCards() {}

  // @GetMapping("/{cardId}")
  // public ResponseEntity<?> getCardByID(@PathVariable String cardId) {}
  @GetMapping("/{cardId}")
  public ResponseEntity<ApiResponse<CardModel>> getCardById(@PathVariable String cardId) {
    CardModel card = cardService.getCardById(cardId); // CardModel을 직접 받음
    ApiResponse<CardModel> response = new ApiResponse<>(true, "success", card);
    return ResponseEntity.ok(response);
  }
}
