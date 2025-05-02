package com.example.stormpaws.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PagedResultDTO<T> {
  private final List<T> items;
  private final int totalItems;
  private final int totalPages;
  private final int currentPage;
  private final int pageSize;
  private final boolean hasPrevious;
  private final boolean hasNext;
}
