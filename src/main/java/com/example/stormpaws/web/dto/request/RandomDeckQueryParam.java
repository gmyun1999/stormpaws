package com.example.stormpaws.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RandomDeckQueryParam(@Min(1) @Max(10) Integer count) {
  public int resolvedCount() {
    return count == null ? 10 : count;
  }
}
