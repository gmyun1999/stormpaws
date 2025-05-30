package com.example.stormpaws.service;

import com.example.stormpaws.domain.IRepository.ICityRepository;
import com.example.stormpaws.domain.IRepository.IWeatherRepository;
import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.domain.model.CityList;
import com.example.stormpaws.domain.model.WeatherLogModel;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import com.example.stormpaws.web.dto.response.OpenMeteoWeatherResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
  private final IWeatherRepository weatherRepository;
  private final ICityRepository cityRepository;
  private final RestTemplate restTemplate;
  private final RedisTemplate<String, CityWeatherInfoDTO> redisTemplate;
  private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";

  public Map<String, Object> getCities() {
    Map<String, WeatherType> cityWeathers = new LinkedHashMap<>();
    Map<WeatherType, Integer> weatherCounts = new EnumMap<>(WeatherType.class);

    for (City city : City.values()) {
      String redisKey = "weather:" + city.name();

      CityWeatherInfoDTO cachedWeather = redisTemplate.opsForValue().get(redisKey);
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

  public CityWeatherInfoDTO getWeather(City city) {
    String redisKey = "weather:" + city.name();

    CityWeatherInfoDTO cachedWeather = redisTemplate.opsForValue().get(redisKey);
    if (cachedWeather != null) {
      log.info("Cache hit for city: {}", city);
      return cachedWeather;
    }

    Optional<WeatherLogModel> weatherLogOpt =
        weatherRepository.findFirstByCityOrderByFetchedAtDesc(city);
    if (weatherLogOpt.isPresent()) {
      WeatherLogModel weatherLog = weatherLogOpt.get();
      CityWeatherInfoDTO cityWeatherInfoDTO = convertToCityWeatherInfoDTO(weatherLog);
      redisTemplate.opsForValue().set(redisKey, cityWeatherInfoDTO, 1, TimeUnit.HOURS);
      log.info("DB hit for city: {}", city);
      return cityWeatherInfoDTO;
    }

    log.info("Fetching weather from API for city: {}", city);
    return fetchAndSaveWeather(city);
  }

  private CityWeatherInfoDTO convertToCityWeatherInfoDTO(WeatherLogModel model) {
    return CityWeatherInfoDTO.builder()
        .city(model.getCity())
        .weatherType(model.getWeatherType())
        .fetchedAt(model.getFetchedAt())
        .build();
  }

  private CityWeatherInfoDTO fetchAndSaveWeather(City city) {
    CityList cityInfo = getCityInfo(city);
    OpenMeteoWeatherResponse.CurrentWeather currentWeather = fetchCurrentWeatherFromAPI(cityInfo);
    WeatherType weatherType = WeatherType.fromExternalCode(currentWeather.getWeathercode());

    CityWeatherInfoDTO cityWeatherInfoDTO =
        CityWeatherInfoDTO.builder()
            .city(city)
            .weatherType(weatherType)
            .fetchedAt(LocalDateTime.now())
            .build();

    saveWeatherLog(cityWeatherInfoDTO);

    String redisKey = "weather:" + city.name();
    redisTemplate.opsForValue().set(redisKey, cityWeatherInfoDTO, 1, TimeUnit.HOURS);

    return cityWeatherInfoDTO;
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

  public List<CityWeatherInfoDTO> fetchAndSaveWeatherBatch(List<City> cities) {
    if (cities == null || cities.isEmpty()) {
      log.warn("No cities provided for batch processing");
      return Collections.emptyList();
    }

    log.info("Starting batch weather fetch for {} cities", cities.size());
    List<CityList> cityInfos =
        cities.stream()
            .map(this::getCityInfo)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (cityInfos.isEmpty()) {
      log.warn("No valid city information found for the provided cities.");
      return Collections.emptyList();
    }

    String latitudes =
        cityInfos.stream()
            .map(c -> String.valueOf(c.getLatitude()))
            .collect(Collectors.joining(","));
    String longitudes =
        cityInfos.stream()
            .map(c -> String.valueOf(c.getLongitude()))
            .collect(Collectors.joining(","));

    String url =
        String.format(
            "%s?latitude=%s&longitude=%s&current_weather=true",
            OPEN_METEO_URL, latitudes, longitudes);
    log.info("Calling Open-Meteo API with URL: {}", url);

    try {
      OpenMeteoWeatherResponse[] responses =
          restTemplate.getForObject(url, OpenMeteoWeatherResponse[].class);

      if (responses == null || responses.length == 0) {
        log.error("Open-Meteo API returned null or empty response for URL: {}", url);
        return Collections.emptyList();
      }

      if (responses.length != cityInfos.size()) {
        log.warn(
            "API response count ({}) and requested city count ({}) differ. URL: {}",
            responses.length,
            cityInfos.size(),
            url);
      }

      List<CityWeatherInfoDTO> results = new ArrayList<>();
      int processCount = Math.min(responses.length, cityInfos.size());
      int successCount = 0;
      int failureCount = 0;

      for (int i = 0; i < processCount; i++) {
        try {
          OpenMeteoWeatherResponse apiResponse = responses[i];
          CityList cityInfo = cityInfos.get(i);

          if (apiResponse == null || apiResponse.getCurrentWeather() == null) {
            log.warn(
                "Skipping city {} due to null response or current_weather data.",
                cityInfo.getCity());
            failureCount++;
            continue;
          }

          OpenMeteoWeatherResponse.CurrentWeather currentWeather = apiResponse.getCurrentWeather();
          WeatherType weatherType = WeatherType.fromExternalCode(currentWeather.getWeathercode());

          CityWeatherInfoDTO cityWeatherInfoDTO =
              CityWeatherInfoDTO.builder()
                  .city(cityInfo.getCity())
                  .weatherType(weatherType)
                  .fetchedAt(LocalDateTime.now())
                  .build();
          results.add(cityWeatherInfoDTO);

          String redisKey = "weather:" + cityInfo.getCity().name();
          redisTemplate.opsForValue().set(redisKey, cityWeatherInfoDTO, 1, TimeUnit.HOURS);

          saveWeatherLog(cityWeatherInfoDTO);
          successCount++;
        } catch (Exception e) {
          log.error("Error processing city {}: {}", cityInfos.get(i).getCity(), e.getMessage(), e);
          failureCount++;
        }
      }

      log.info(
          "Batch weather fetch completed. Success: {}, Failure: {}", successCount, failureCount);
      return results;
    } catch (Exception e) {
      log.error("Error fetching weather data from Open-Meteo API: {}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private void saveWeatherLog(CityWeatherInfoDTO cityWeatherInfoDTO) {
    WeatherLogModel weatherLog =
        WeatherLogModel.builder()
            .id(UUID.randomUUID().toString())
            .city(cityWeatherInfoDTO.getCity())
            .weatherType(cityWeatherInfoDTO.getWeatherType())
            .fetchedAt(cityWeatherInfoDTO.getFetchedAt())
            .build();

    try {
      weatherRepository.save(weatherLog);
      log.info(
          "Weather log saved successfully for city {}: {}",
          cityWeatherInfoDTO.getCity(),
          weatherLog);
    } catch (Exception e) {
      log.error("DB 저장 중 오류 발생 for city {}: {}", cityWeatherInfoDTO.getCity(), e.getMessage(), e);
    }
  }

  public Optional<WeatherLogModel> getLatestWeather(City city) {
    return weatherRepository.findFirstByCityOrderByFetchedAtDesc(city);
  }

  public List<WeatherLogModel> getAllWeatherByCity(City city) {
    return weatherRepository.findAllByCityOrderByFetchedAtDesc(city);
  }

  public void saveWeather(CityWeatherInfoDTO cityWeatherInfoDTO) {
    String redisKey = "weather:" + cityWeatherInfoDTO.getCity().name();
    redisTemplate.opsForValue().set(redisKey, cityWeatherInfoDTO, 1, TimeUnit.HOURS);
    saveWeatherLog(cityWeatherInfoDTO);
  }
}
