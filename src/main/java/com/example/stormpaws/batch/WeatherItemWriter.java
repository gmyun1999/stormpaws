package com.example.stormpaws.batch;

import com.example.stormpaws.service.WeatherService;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherItemWriter implements ItemWriter<List<CityWeatherInfoDTO>> {
  private final WeatherService weatherService;

  public WeatherItemWriter(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  @Override
  public void write(Chunk<? extends List<CityWeatherInfoDTO>> items) throws Exception {
    int totalProcessed = 0;
    int totalFailed = 0;

    for (List<CityWeatherInfoDTO> batch : items) {
      for (CityWeatherInfoDTO weather : batch) {
        try {
          weatherService.saveWeather(weather);
          totalProcessed++;
        } catch (Exception e) {
          log.error("날씨 데이터 저장 실패 for city {}: {}", weather.getCity(), e.getMessage());
          totalFailed++;
        }
      }
    }

    log.info("배치 저장 완료: 성공={}, 실패={}", totalProcessed, totalFailed);

    if (totalFailed > 0) {
      throw new RuntimeException(String.format("배치 저장 중 %d개의 항목이 실패했습니다", totalFailed));
    }
  }
}
