package com.example.stormpaws.domain.constant;

public enum WeatherType {
  CLEAR,
  CLOUDS,
  RAIN,
  SNOW,
  FOG,
  MIST,
  THUNDERSTORM,
  DUST,
  TORNADO,
  GUST,
  UNKNOWN;

  public static WeatherType fromExternalCode(int code) {
    return switch (code) {
      case 0 -> CLEAR;
      case 1, 2, 3 -> CLOUDS;
      case 45 -> MIST; // 안개
      case 48 -> FOG; // 짙은 안개
      case 51, 53, 55, 61, 63, 65, 80, 81, 82 -> RAIN;
      case 71, 73, 75, 77 -> SNOW;
      case 95, 96, 99 -> THUNDERSTORM;
      case 6 -> DUST; // 먼지
      case 7 -> GUST; // 강풍
      case 8, 9 -> TORNADO; // 회오리
      default -> UNKNOWN;
    };
  }
}
