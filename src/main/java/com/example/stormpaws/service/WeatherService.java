// WeatherService 클래스
package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.ICityRepository;
import com.example.stormpaws.domain.IRepository.IWeatherRepository;
import com.example.stormpaws.domain.model.CityModel;
import com.example.stormpaws.domain.model.WeatherLogModel;
import com.example.stormpaws.service.weatherAPI.WeatherApiClient;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherService {

  private final ICityRepository cityRepository;
  private final IWeatherRepository weatherRepository;
  private final WeatherApiClient weatherApiClient;

  public WeatherService(
      ICityRepository cityRepository,
      IWeatherRepository weatherRepository,
      WeatherApiClient weatherApiClient) {
    this.cityRepository = cityRepository;
    this.weatherRepository = weatherRepository;
    this.weatherApiClient = weatherApiClient;
  }

  @Transactional
  public void updateAllCitiesWeather() {
    // 1) DB에서 모든 CityModel 가져오기
    List<CityModel> cities = cityRepository.findAll();

    if (cities.isEmpty()) {
      return;
    }

    // 2) OpenMeteo API 호출하여 WeatherLogModel 리스트 생성
    List<WeatherLogModel> weatherLogs = weatherApiClient.getCurrentWeatherBulk(cities);

    // 3) bulk 저장.
    weatherRepository.saveAll(weatherLogs);
  }
}
