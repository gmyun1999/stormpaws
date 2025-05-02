package com.example.stormpaws.web.controller;

import com.example.stormpaws.domain.model.DeckModel;
import com.example.stormpaws.infra.security.CustomUserDetails;
import com.example.stormpaws.service.DeckService;
import com.example.stormpaws.service.dto.DeckCardDTO;
import com.example.stormpaws.service.dto.PagedResultDTO;
import com.example.stormpaws.web.dto.request.CreateDeckBody;
import com.example.stormpaws.web.dto.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DeckController {

  private final DeckService deckService;

  public DeckController(DeckService deckService) {
    this.deckService = deckService;
  }

  @GetMapping("/decks/{deckId}")
  public ResponseEntity<ApiResponse<DeckModel>> getDeckByID(@PathVariable String deckId) {
    DeckModel deck = deckService.getDeckById(deckId);
    ApiResponse<DeckModel> response = new ApiResponse<>(true, "success", deck);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/user/me/decks")
  public ResponseEntity<ApiResponse<PagedResultDTO<DeckModel>>> getMyDecks(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    String userId = userDetails.getUser().getId();
    PagedResultDTO<DeckModel> result = deckService.getMyDeckList(userId, page, size);
    ApiResponse<PagedResultDTO<DeckModel>> response = new ApiResponse<>(true, "success", result);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/user/me/decks")
  public ResponseEntity<ApiResponse<DeckModel>> createDeck(
      @Valid @RequestBody CreateDeckBody requestDTO,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    DeckCardDTO deckCardDTO = mapToDeckCardDTO(requestDTO);
    DeckModel deckModel = deckService.createDeck(deckCardDTO, userDetails.getUser());
    ApiResponse<DeckModel> response =
        new ApiResponse<>(true, "create deck successfully", deckModel);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // @GetMapping("/decks/random")
  // public ResponseEntity<ApiResponse<List<DeckModel>>> getRandomDecks(
  //     @Valid @ModelAttribute RandomDeckQueryParam  count,
  //     @AuthenticationPrincipal CustomUserDetails userDetails
  //   ) {

  //   List<DeckModel> result = deckService.getRandomDecks(count, userDetails.getUser());
  //   ApiResponse<List<DeckModel>> response = new ApiResponse<>(true, "success", result);
  //   return ResponseEntity.ok(response);
  // }

  private DeckCardDTO mapToDeckCardDTO(CreateDeckBody requestDTO) {
    List<DeckCardDTO.CardDTO> cardDTOList =
        requestDTO.cards().stream()
            .map(card -> new DeckCardDTO.CardDTO(card.cardId(), card.quantity(), card.pos()))
            .collect(Collectors.toList());

    return new DeckCardDTO(requestDTO.name(), cardDTOList);
  }
}
