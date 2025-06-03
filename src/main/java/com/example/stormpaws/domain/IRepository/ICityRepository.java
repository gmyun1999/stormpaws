package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.model.CityList;
import java.util.Optional;

public interface ICityRepository {

  Optional<CityList> findByCity(City city);

  <S extends CityList> S save(S entity);
}
