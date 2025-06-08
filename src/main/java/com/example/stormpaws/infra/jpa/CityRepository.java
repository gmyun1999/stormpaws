package com.example.stormpaws.infra.jpa;

import com.example.stormpaws.domain.IRepository.ICityRepository;
import com.example.stormpaws.domain.model.CityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<CityModel, String>, ICityRepository {}
