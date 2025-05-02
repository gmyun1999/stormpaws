package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IDeckCardRepository;
import com.example.stormpaws.domain.model.DeckCardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckCardRepository
    extends JpaRepository<DeckCardModel, String>, IDeckCardRepository {}
