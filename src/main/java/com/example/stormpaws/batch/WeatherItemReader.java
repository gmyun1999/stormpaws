package com.example.stormpaws.batch;

import com.example.stormpaws.domain.constant.City;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemReader implements ItemReader<List<City>> {
  private final List<City> cities = Arrays.asList(City.values());
  private int currentIndex = 0;
  private static final int BATCH_SIZE = 5;
  private boolean isCompleted = false;

  @Override
  public List<City> read() {
    if (isCompleted || currentIndex >= cities.size()) {
      isCompleted = true;
      return null;
    }

    int endIndex = Math.min(currentIndex + BATCH_SIZE, cities.size());
    List<City> batch = new ArrayList<>(cities.subList(currentIndex, endIndex));
    currentIndex = endIndex;

    if (currentIndex >= cities.size()) {
      isCompleted = true;
    }

    return batch;
  }
}
