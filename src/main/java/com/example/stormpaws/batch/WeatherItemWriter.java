package com.example.stormpaws.batch;

import com.example.stormpaws.service.WeatherService;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.List;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemWriter implements ItemWriter<List<CityWeatherInfoDTO>> {
  private final WeatherService weatherService;

  public WeatherItemWriter(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  @Override
  public void write(Chunk<? extends List<CityWeatherInfoDTO>> items) throws Exception {
    for (List<CityWeatherInfoDTO> batch : items) {
      for (CityWeatherInfoDTO weather : batch) {
        weatherService.saveWeather(weather);
      }
    }
  }
}
