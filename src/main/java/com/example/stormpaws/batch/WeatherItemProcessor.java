package com.example.stormpaws.batch;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.service.WeatherService;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherItemProcessor implements ItemProcessor<List<City>, List<CityWeatherInfoDTO>> {
  private final WeatherService weatherService;

  public WeatherItemProcessor(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  @Override
  public List<CityWeatherInfoDTO> process(List<City> cities) throws Exception {
    if (cities == null || cities.isEmpty()) {
      log.warn("처리할 도시 목록이 비어있습니다");
      return null;
    }

    log.info("배치 처리 시작: {} 개의 도시", cities.size());
    List<CityWeatherInfoDTO> results = new ArrayList<>();

    for (City city : cities) {
      try {
        CityWeatherInfoDTO weatherInfo = weatherService.fetchWeatherFromAPI(city);
        if (weatherInfo != null) {
          results.add(weatherInfo);
        }
      } catch (Exception e) {
        log.error("날씨 데이터 처리 실패 for city {}: {}", city, e.getMessage());
      }
    }

    if (results.isEmpty()) {
      log.warn("처리된 날씨 데이터가 없습니다: {}", cities);
      return null;
    }

    log.info("배치 처리 완료: {} 개의 도시 처리됨", results.size());
    return results;
  }
}
