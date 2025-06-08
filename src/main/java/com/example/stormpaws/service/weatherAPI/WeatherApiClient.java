package com.example.stormpaws.service.weatherAPI;

import com.example.stormpaws.domain.model.CityModel;
import com.example.stormpaws.domain.model.WeatherLogModel;
import java.util.List;

public interface WeatherApiClient {
  /** 여러 개의 CityList를 받아서 한 번의 호출로 각 도시별 현재 날씨 정보를 반환한다. */
  List<WeatherLogModel> getCurrentWeatherBulk(List<CityModel> cityInfos);
}
