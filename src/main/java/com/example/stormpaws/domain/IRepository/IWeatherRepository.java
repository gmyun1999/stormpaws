package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.model.WeatherLogModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IWeatherRepository extends JpaRepository<WeatherLogModel, String> {
  Optional<WeatherLogModel> findFirstByCityOrderByFetchedAtDesc(City city);

  List<WeatherLogModel> findAllByCityOrderByFetchedAtDesc(City city);
}
