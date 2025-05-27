package com.example.stormpaws.domain.model;

import com.example.stormpaws.domain.constant.City;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CityList {
  @Id private String id;

  @Enumerated(EnumType.STRING)
  private City city;

  private double latitude;
  private double longitude;
}
