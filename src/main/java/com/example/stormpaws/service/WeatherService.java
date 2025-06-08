package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.ICityRepository;
import com.example.stormpaws.domain.IRepository.IWeatherRepository;
import com.example.stormpaws.domain.model.CityModel;
import com.example.stormpaws.domain.model.WeatherLogModel;
import com.example.stormpaws.service.weatherAPI.WeatherApiClient;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherService {

  private final ICityRepository cityRepository;
  private final IWeatherRepository weatherRepository;
  private final WeatherApiClient weatherApiClient;
  private final Random random = new Random();

  public WeatherService(
      ICityRepository cityRepository,
      IWeatherRepository weatherRepository,
      WeatherApiClient weatherApiClient) {
    this.cityRepository = cityRepository;
    this.weatherRepository = weatherRepository;
    this.weatherApiClient = weatherApiClient;
  }

  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  @Transactional
  public void updateAllCitiesWeather() {
    // 모든 CityModel 가져오기
    List<CityModel> cities = cityRepository.findAll();

    if (cities.isEmpty()) {
      return;
    }

    // OpenMeteo API 호출하여 WeatherLogModel 리스트 생성
    List<WeatherLogModel> weatherLogs = weatherApiClient.getCurrentWeatherBulk(cities);

    // bulk 저장
    weatherRepository.saveAll(weatherLogs);
  }

  @Transactional(readOnly = true)
  public WeatherLogModel getRandomCityLatestWeather() {
    // 모든 city ID만 조회
    List<String> cityIds = cityRepository.findAllCityIds();
    if (cityIds.isEmpty()) {
      throw new IllegalStateException("등록된 도시가 없습니다.");
    }

    // 섞어서 하나씩 시도
    Collections.shuffle(cityIds, random);
    for (String cityId : cityIds) {
      CityModel cityModel = cityRepository.findById(cityId).orElse(null);
      if (cityModel == null) continue;

      Optional<WeatherLogModel> optLog =
          weatherRepository.findFirstByCityOrderByFetchedAtDesc(cityModel.getCity());
      if (optLog.isPresent()) {
        return optLog.get();
      }
    }

    throw new IllegalStateException("어떤 도시도 최신 날씨 로그가 없습니다.");
  }
}
