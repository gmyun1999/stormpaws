package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.ICardRepository;
import com.example.stormpaws.domain.model.CardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<CardModel, String>, ICardRepository {}
