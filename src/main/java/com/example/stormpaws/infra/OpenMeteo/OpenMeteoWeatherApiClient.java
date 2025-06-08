package com.example.stormpaws.infra.OpenMeteo;

import com.example.stormpaws.domain.constant.WeatherType;
import com.example.stormpaws.domain.model.CityModel;
import com.example.stormpaws.domain.model.WeatherLogModel;
import com.example.stormpaws.infra.OpenMeteo.responseDTO.OpenMeteoBulkWeatherResponse;
import com.example.stormpaws.infra.OpenMeteo.responseDTO.OpenMeteoCurrentWeather;
import com.example.stormpaws.service.weatherAPI.WeatherApiClient;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenMeteoWeatherApiClient implements WeatherApiClient {

  private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
  private final RestTemplate restTemplate;

  public OpenMeteoWeatherApiClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public List<WeatherLogModel> getCurrentWeatherBulk(List<CityModel> cityInfos) {
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

    ResponseEntity<List<OpenMeteoBulkWeatherResponse>> responseEntity =
        restTemplate.exchange(
            URI.create(url),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<OpenMeteoBulkWeatherResponse>>() {});

    List<OpenMeteoBulkWeatherResponse> bulkResponse = responseEntity.getBody();
    if (bulkResponse == null || bulkResponse.isEmpty()) {
      return new ArrayList<>();
    }

    List<WeatherLogModel> result = new ArrayList<>();
    for (int i = 0; i < bulkResponse.size(); i++) {
      OpenMeteoBulkWeatherResponse entry = bulkResponse.get(i);
      OpenMeteoCurrentWeather cw = entry.getCurrentWeather();
      CityModel cityModel = cityInfos.get(i);

      if (cw == null) continue;

      WeatherType weatherType = mapToWeatherType(cw.getWeatherCode());
      LocalDateTime fetchedAt = LocalDateTime.parse(cw.getTime());

      result.add(
          WeatherLogModel.builder()
              .id(UUID.randomUUID().toString())
              .city(cityModel.getCity())
              .weatherType(weatherType)
              .fetchedAt(fetchedAt)
              .build());
    }

    return result;
  }

  private WeatherType mapToWeatherType(int code) {
    return switch (code) {
      case 0 -> WeatherType.CLEAR;
      case 1, 2, 3 -> WeatherType.CLOUDS;
      case 45 -> WeatherType.MIST;
      case 48 -> WeatherType.FOG;
      case 51, 53, 55, 61, 63, 65, 80, 81, 82 -> WeatherType.RAIN;
      case 71, 73, 75, 77 -> WeatherType.SNOW;
      case 95, 96, 99 -> WeatherType.THUNDERSTORM;
      case 6 -> WeatherType.DUST;
      case 7 -> WeatherType.GUST;
      case 8, 9 -> WeatherType.TORNADO;
      default -> WeatherType.UNKNOWN;
    };
  }
}
