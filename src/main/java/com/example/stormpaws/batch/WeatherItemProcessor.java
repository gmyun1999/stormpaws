package com.example.stormpaws.batch;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.service.WeatherService;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.List;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemProcessor implements ItemProcessor<List<City>, List<CityWeatherInfoDTO>> {
  private final WeatherService weatherService;

  public WeatherItemProcessor(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  @Override
  public List<CityWeatherInfoDTO> process(List<City> cities) throws Exception {
    return weatherService.fetchAndSaveWeatherBatch(cities);
  }
}
