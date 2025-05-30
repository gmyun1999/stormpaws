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
  private static final int BATCH_SIZE = 100;

  @Override
  public List<City> read() {
    if (currentIndex >= cities.size()) {
      return null;
    }

    int endIndex = Math.min(currentIndex + BATCH_SIZE, cities.size());
    List<City> batch = new ArrayList<>(cities.subList(currentIndex, endIndex));
    currentIndex = endIndex;
    return batch;
  }
}
