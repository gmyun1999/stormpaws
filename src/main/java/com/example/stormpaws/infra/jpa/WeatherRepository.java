package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.model.WeatherLogModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<WeatherLogModel, String> {
  Optional<WeatherLogModel> findFirstByCityOrderByFetchedAtDesc(City city);

  List<WeatherLogModel> findAllByCityOrderByFetchedAtDesc(City city);
}
