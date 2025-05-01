package com.example.stormpaws.service;

import com.example.stormpaws.service.dto.PagedResultDTO;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Paginator {

  private static final int MAX_PAGE_SIZE = 100;

  public static <T> PagedResultDTO<T> paginate(List<T> items, int page, int pageSize) {
    if (page < 1 || pageSize < 1) {
      throw new IllegalArgumentException("페이지 번호와 크기는 1 이상이어야 합니다.");
    }

    if (pageSize > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("페이지 크기는 최대 " + MAX_PAGE_SIZE + "까지 가능합니다.");
    }

    int totalItems = items.size();
    int totalPages = (int) Math.ceil((double) totalItems / pageSize);

    if (page > totalPages && totalPages != 0) {
      throw new IllegalArgumentException("페이지 번호가 총 페이지 수를 초과합니다.");
    }

    int fromIndex = (page - 1) * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, totalItems);
    List<T> pagedItems =
        fromIndex >= totalItems ? Collections.emptyList() : items.subList(fromIndex, toIndex);

    return new PagedResultDTO<>(
        pagedItems, totalItems, totalPages, page, pageSize, page > 1, page < totalPages);
  }
}
