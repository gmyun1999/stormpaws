package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.model.WeatherLogModel;
import java.util.List;
import java.util.Optional;

public interface IWeatherRepository {

  Optional<WeatherLogModel> findFirstByCityOrderByFetchedAtDesc(City city);

  List<WeatherLogModel> findAllByCityOrderByFetchedAtDesc(City city);

  <S extends WeatherLogModel> S save(S entity);

  <S extends WeatherLogModel> List<S> saveAll(Iterable<S> entities);
}
