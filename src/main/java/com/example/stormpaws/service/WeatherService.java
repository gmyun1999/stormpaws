package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.ICityRepository;
import com.example.stormpaws.domain.IRepository.IWeatherRepository;
import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.domain.model.CityList;
import com.example.stormpaws.domain.model.WeatherLogModel;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import com.example.stormpaws.web.dto.response.OpenMeteoWeatherResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
  private final IWeatherRepository weatherRepository;
  private final ICityRepository cityRepository;
  private final RestTemplate restTemplate;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_DELAY_MS = 1000;
  private static final long CACHE_EXPIRATION_HOURS = 24;

  private CityWeatherInfoDTO getWeatherInfoFromCacheOrDB(City city) {
    String redisKey = "weather:" + city.name();
    Object redisValue = redisTemplate.opsForValue().get(redisKey);
    CityWeatherInfoDTO cachedWeather = null;
    if (redisValue instanceof CityWeatherInfoDTO) {
      cachedWeather = (CityWeatherInfoDTO) redisValue;
    } else if (redisValue instanceof java.util.Map) {
      cachedWeather = objectMapper.convertValue(redisValue, CityWeatherInfoDTO.class);
    }
    if (cachedWeather != null) {
      return cachedWeather;
    }

    Optional<WeatherLogModel> weatherLogOpt =
        weatherRepository.findFirstByCityOrderByFetchedAtDesc(city);
    if (weatherLogOpt.isPresent()) {
      WeatherLogModel weatherLog = weatherLogOpt.get();
      return convertToCityWeatherInfoDTO(weatherLog);
    }
    return null;
  }

  public Map<String, Object> getCities() {
    Map<String, WeatherType> cityWeathers = new LinkedHashMap<>();
    Map<WeatherType, Integer> weatherCounts = new EnumMap<>(WeatherType.class);

    for (City city : City.values()) {
      CityWeatherInfoDTO weatherInfo = getWeatherInfoFromCacheOrDB(city);
      if (weatherInfo != null) {
        cityWeathers.put(city.name(), weatherInfo.getWeatherType());
        weatherCounts.merge(weatherInfo.getWeatherType(), 1, Integer::sum);
      } else {
        cityWeathers.put(city.name(), null);
      }
    }

    int totalCities = City.values().length;
    Map<WeatherType, Integer> weatherProbabilities = new EnumMap<>(WeatherType.class);
    for (WeatherType type : WeatherType.values()) {
      int count = weatherCounts.getOrDefault(type, 0);
      double probabilityPercent = totalCities > 0 ? ((double) count * 100) / totalCities : 0.0;
      weatherProbabilities.put(type, (int) Math.round(probabilityPercent));
    }

    Map<String, Object> summaryResult = new HashMap<>();
    summaryResult.put("cityWeathers", cityWeathers);
    summaryResult.put("weatherProbabilities", weatherProbabilities);

    return summaryResult;
  }

  public CityWeatherInfoDTO fetchWeather(City city) {
    try {
      CityWeatherInfoDTO weatherInfo = getWeatherInfoFromCacheOrDB(city);
      if (weatherInfo != null) {
        return weatherInfo;
      }
      return fetchWeatherFromAPI(city);
    } catch (Exception e) {
      throw new RuntimeException("failed to fetch weather info: " + e.getMessage(), e);
    }
  }

  public CityWeatherInfoDTO fetchWeatherFromAPI(City city) {
    int retryCount = 0;
    while (retryCount < MAX_RETRIES) {
      try {
        CityList cityInfo = getCityInfo(city);
        OpenMeteoWeatherResponse.CurrentWeather currentWeather =
            fetchCurrentWeatherFromAPI(cityInfo);
        WeatherType weatherType = WeatherType.fromExternalCode(currentWeather.getWeathercode());

        return CityWeatherInfoDTO.builder()
            .city(city)
            .weatherType(weatherType)
            .fetchedAt(LocalDateTime.now())
            .build();
      } catch (Exception e) {
        retryCount++;
        if (retryCount == MAX_RETRIES) {
          log.error("최대 재시도 횟수 초과 for city {}: {}", city, e.getMessage());
          throw new RuntimeException("날씨 정보를 가져오는데 실패했습니다: " + e.getMessage(), e);
        }
        log.warn("재시도 {} for city {}: {}", retryCount, city, e.getMessage());
        try {
          Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("재시도 중 인터럽트 발생", ie);
        }
      }
    }
    throw new RuntimeException("날씨 정보를 가져오는데 실패했습니다");
  }

  private CityList getCityInfo(City city) {
    Optional<CityList> cityInfo = cityRepository.findByCity(city);
    if (cityInfo.isPresent()) {
      return cityInfo.get();
    }
    throw new RuntimeException("City not found: " + city);
  }

  private OpenMeteoWeatherResponse.CurrentWeather fetchCurrentWeatherFromAPI(CityList cityInfo) {
    String url =
        String.format(
            "%s?latitude=%f&longitude=%f&current_weather=true",
            OPEN_METEO_URL, cityInfo.getLatitude(), cityInfo.getLongitude());
    log.info("Calling Open-Meteo API with URL: {}", url);

    try {
      OpenMeteoWeatherResponse response =
          restTemplate.getForObject(url, OpenMeteoWeatherResponse.class);
      if (response == null) {
        log.error("Open-Meteo API returned null response for URL: {}", url);
        throw new RuntimeException("Open-Meteo API response is null");
      }
      if (response.getCurrentWeather() == null) {
        log.error("Open-Meteo API response has no current weather data for URL: {}", url);
        throw new RuntimeException("Open-Meteo API response has no current weather data");
      }
      log.info(
          "Successfully fetched weather data for city {}: {}",
          cityInfo.getCity(),
          response.getCurrentWeather());
      return response.getCurrentWeather();
    } catch (Exception e) {
      log.error(
          "Error fetching weather data from Open-Meteo API for city {}: {}",
          cityInfo.getCity(),
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to fetch weather data: " + e.getMessage(), e);
    }
  }

  @Transactional
  public void saveWeather(CityWeatherInfoDTO cityWeatherInfoDTO) {

    try {
      // DB 저장
      WeatherLogModel weatherLog =
          WeatherLogModel.builder()
              .id(UUID.randomUUID().toString())
              .city(cityWeatherInfoDTO.getCity())
              .weatherType(cityWeatherInfoDTO.getWeatherType())
              .fetchedAt(cityWeatherInfoDTO.getFetchedAt())
              .build();

      weatherRepository.save(weatherLog);
      log.info(
          "Weather log saved successfully for city {}: {}",
          cityWeatherInfoDTO.getCity(),
          weatherLog);

      // Redis 캐싱
      String redisKey = "weather:" + cityWeatherInfoDTO.getCity().name();
      try {
        redisTemplate
            .opsForValue()
            .set(redisKey, cityWeatherInfoDTO, CACHE_EXPIRATION_HOURS, TimeUnit.HOURS);
        log.info("Weather data cached in Redis for city: {}", cityWeatherInfoDTO.getCity());
      } catch (Exception e) {
        log.error("Redis 캐싱 실패 for city {}: {}", cityWeatherInfoDTO.getCity(), e.getMessage());
        // Redis 캐싱 실패는 전체 트랜잭션을 실패시키지 않음
      }
    } catch (Exception e) {
      log.error("날씨 데이터 저장 실패 for city {}: {}", cityWeatherInfoDTO.getCity(), e.getMessage());
      throw new RuntimeException("날씨 데이터 저장 실패: " + e.getMessage(), e);
    }
  }

  private CityWeatherInfoDTO convertToCityWeatherInfoDTO(WeatherLogModel model) {
    if (model == null) {
      throw new IllegalArgumentException("WeatherLogModel cannot be null");
    }

    return CityWeatherInfoDTO.builder()
        .city(model.getCity())
        .weatherType(model.getWeatherType())
        .fetchedAt(model.getFetchedAt())
        .build();
  }
}
