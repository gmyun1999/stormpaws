package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.model.CityModel;
import java.util.List;
import java.util.Optional;

public interface ICityRepository {

  Optional<CityModel> findByCity(City city);

  <S extends CityModel> S save(S entity);

  List<CityModel> findAll();

  List<String> findAllCityIds();

  Optional<CityModel> findById(String id);
}
