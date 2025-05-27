package com.example.stormpaws.domain.IRepository;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.model.CityList;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICityRepository extends JpaRepository<CityList, String> {
  Optional<CityList> findByCity(City city);
}
