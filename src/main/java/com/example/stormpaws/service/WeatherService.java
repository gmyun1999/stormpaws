package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.IWeatherRepository;
import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.model.WeatherLogModel;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {
  private final IWeatherRepository weatherRepository;

  public Optional<WeatherLogModel> getLatestWeather(City city) {
    return weatherRepository.findFirstByCityOrderByFetchedAtDesc(city);
  }

  public List<WeatherLogModel> getAllWeatherByCity(City city) {
    return weatherRepository.findAllByCityOrderByFetchedAtDesc(city);
  }
}
