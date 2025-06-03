package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.IWeatherRepository;
import com.example.stormpaws.domain.model.WeatherLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository
    extends JpaRepository<WeatherLogModel, String>, IWeatherRepository {}
