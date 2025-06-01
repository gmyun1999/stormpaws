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
import java.util.List;
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
  private static final long CACHE_EXPIRATION_HOURS = 1;

  public Map<String, Object> getCities() {
    Map<String, WeatherType> cityWeathers = new LinkedHashMap<>();
    Map<WeatherType, Integer> weatherCounts = new EnumMap<>(WeatherType.class);

    for (City city : City.values()) {
      String redisKey = "weather:" + city.name();

      Object redisValue = redisTemplate.opsForValue().get(redisKey);
      CityWeatherInfoDTO cachedWeather = null;
      if (redisValue instanceof CityWeatherInfoDTO) {
        cachedWeather = (CityWeatherInfoDTO) redisValue;
      } else if (redisValue instanceof java.util.Map) {
        cachedWeather = objectMapper.convertValue(redisValue, CityWeatherInfoDTO.class);
      }
      if (cachedWeather != null) {
        cityWeathers.put(city.name(), cachedWeather.getWeatherType());
        weatherCounts.merge(cachedWeather.getWeatherType(), 1, Integer::sum);
        continue;
      }

      Optional<WeatherLogModel> weatherLogOpt =
          weatherRepository.findFirstByCityOrderByFetchedAtDesc(city);
      if (weatherLogOpt.isPresent()) {
        WeatherLogModel weatherLog = weatherLogOpt.get();
        CityWeatherInfoDTO cityWeatherInfoDTO = convertToCityWeatherInfoDTO(weatherLog);
        cityWeathers.put(city.name(), cityWeatherInfoDTO.getWeatherType());
        weatherCounts.merge(cityWeatherInfoDTO.getWeatherType(), 1, Integer::sum);
        redisTemplate.opsForValue().set(redisKey, cityWeatherInfoDTO, 1, TimeUnit.HOURS);
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
    String redisKey = "weather:" + city.name();
    try {
      // 1. Redis에서 먼저 확인
      Object redisValue = redisTemplate.opsForValue().get(redisKey);
      CityWeatherInfoDTO cachedWeather = null;
      if (redisValue instanceof CityWeatherInfoDTO) {
        cachedWeather = (CityWeatherInfoDTO) redisValue;
      } else if (redisValue instanceof java.util.Map) {
        cachedWeather = objectMapper.convertValue(redisValue, CityWeatherInfoDTO.class);
      }
      if (cachedWeather != null) {
        log.info("Cache hit for city: {}", city);
        return cachedWeather;
      }

      // 2. DB에서 확인
      Optional<WeatherLogModel> weatherLogOpt =
          weatherRepository.findFirstByCityOrderByFetchedAtDesc(city);
      if (weatherLogOpt.isPresent()) {
        WeatherLogModel weatherLog = weatherLogOpt.get();
        CityWeatherInfoDTO cityWeatherInfoDTO = convertToCityWeatherInfoDTO(weatherLog);
        log.info("DB hit for city: {}", city);
        return cityWeatherInfoDTO;
      }

      // 3. API에서 가져오기
      log.info("Fetching weather from API for city: {}", city);
      return fetchWeatherFromAPI(city);
    } catch (Exception e) {
      log.error("날씨 정보 조회 실패 for city {}: {}", city, e.getMessage());
      throw new RuntimeException("날씨 정보를 가져오는데 실패했습니다: " + e.getMessage(), e);
    }
  }

  private CityWeatherInfoDTO fetchWeatherFromAPI(City city) {
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
    log.info("Fetching city info for: {}", city);
    Optional<CityList> cityInfo = cityRepository.findByCity(city);
    if (cityInfo.isPresent()) {
      log.info("Found city info: {}", cityInfo.get());
      return cityInfo.get();
    }
    log.error("City not found in database: {}", city);
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

  /* @Transactional
   public List<CityWeatherInfoDTO> fetchAndSaveWeatherBatch(List<City> cities) {
     if (cities == null || cities.isEmpty()) {
       log.warn("No cities provided for batch processing");
       return Collections.emptyList();
     }

     log.info("Starting batch weather fetch for {} cities", cities.size());
     List<CityWeatherInfoDTO> results = new ArrayList<>();
     List<Exception> errors = new ArrayList<>();

     for (City city : cities) {
       try {
         CityWeatherInfoDTO weatherInfo = fetchWeather(city);
         results.add(weatherInfo);
       } catch (Exception e) {
         log.error("Error processing city {}: {}", city, e.getMessage());
         errors.add(e);
       }
     }

     if (!errors.isEmpty()) {
       log.warn(
           "Batch processing completed with {} errors out of {} cities",
           errors.size(),
           cities.size());
     }

     log.info("Batch weather fetch completed. Processed {} cities", results.size());
     return results;
   }
  */
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

  public Optional<WeatherLogModel> getLatestWeather(City city) {
    return weatherRepository.findFirstByCityOrderByFetchedAtDesc(city);
  }

  public List<WeatherLogModel> getAllWeatherByCity(City city) {
    return weatherRepository.findAllByCityOrderByFetchedAtDesc(city);
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
