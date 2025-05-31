package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.IDeckRepository;
import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.domain.model.CardModel;
import com.example.stormpaws.domain.model.DeckCardModel;
import com.example.stormpaws.domain.model.DeckModel;
import com.example.stormpaws.service.BattleSimulator.Unit;
import com.example.stormpaws.service.dto.BattleResultDTO;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BattleService {

  private final IDeckRepository deckRepo;
  private final BattleSimulator simulator;

  public BattleService(IDeckRepository deckRepo, BattleSimulator simulator) {
    this.deckRepo = deckRepo;
    this.simulator = simulator;
  }

  @Transactional
  public BattleResultDTO runSimulation(
      String attackerDeckId, String defenderDeckId, WeatherType weather) {
    DeckModel aDeck =
        deckRepo
            .findByIdWithDeckCardsAndCards(attackerDeckId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid deck ID: " + attackerDeckId));

    DeckModel bDeck =
        deckRepo
            .findByIdWithDeckCardsAndCards(defenderDeckId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid deck ID: " + defenderDeckId));

    List<Unit> attackers = toUnits(aDeck, weather);
    List<Unit> defenders = toUnits(bDeck, weather);
    return simulator.simulate(attackerDeckId, attackers, defenderDeckId, defenders);
  }

  private List<Unit> toUnits(DeckModel deck, WeatherType weather) {
    var cards = new ArrayList<>(deck.getDecklist());
    cards.sort(Comparator.comparingInt(DeckCardModel::getPos));

    List<Unit> units = new ArrayList<>();
    for (var dc : cards) {
      CardModel card = dc.getCard();
      int baseAttack = card.getAttackPower();
      boolean match = card.getCardType().equalsIgnoreCase(weather.name());
      int effectiveAttack = match ? baseAttack * card.getAdditionalCoefficient() : baseAttack;
      for (int i = 0; i < dc.getCardQuantity(); i++) {
        units.add(new Unit(card.getId(), card.getHealth(), effectiveAttack, card.getAttackSpeed()));
      }
    }
    return units;
  }
}
