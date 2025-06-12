package com.example.stormpaws.web.dto.request;

import java.util.List;

public record DeleteDeckRequest(List<String> deckIds) {}
