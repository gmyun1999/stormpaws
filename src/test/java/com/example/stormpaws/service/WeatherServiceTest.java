// package com.example.stormpaws.service;

// import static org.mockito.Mockito.when;

// import com.example.stormpaws.domain.IRepository.IWeatherRepository;
// import com.example.stormpaws.domain.constant.City;
// import com.example.stormpaws.domain.constant.WeatherType;
// import com.example.stormpaws.domain.model.WeatherLogModel;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// @ExtendWith(MockitoExtension.class)
// class WeatherServiceTest {

//   @Mock private IWeatherRepository weatherRepository;

//   @InjectMocks private WeatherService weatherService;

//   @Test
//   void getLatestWeather_ShouldReturnLatestWeather() {
//     // Given
//     City city = City.SEOUL;
//     WeatherLogModel expectedWeather =
//         WeatherLogModel.builder()
//             .id("test-id")
//             .city(city)
//             .weatherType(WeatherType.CLEAR)
//             .fetchedAt(LocalDateTime.now())
//             .build();

//     when(weatherRepository.findFirstByCityOrderByFetchedAtDesc(city))
//         .thenReturn(Optional.of(expectedWeather));

//     // Then
//   }

//   @Test
//   void getAllWeatherByCity_ShouldReturnAllWeather() {
//     // Given
//     City city = City.SEOUL;
//     List<WeatherLogModel> expectedWeathers =
//         Arrays.asList(
//             WeatherLogModel.builder()
//                 .id("test-id-1")
//                 .city(city)
//                 .weatherType(WeatherType.CLEAR)
//                 .fetchedAt(LocalDateTime.now())
//                 .build(),
//             WeatherLogModel.builder()
//                 .id("test-id-2")
//                 .city(city)
//                 .weatherType(WeatherType.RAIN)
//                 .fetchedAt(LocalDateTime.now().minusHours(1))
//                 .build());

//     when(weatherRepository.findAllByCityOrderByFetchedAtDesc(city)).thenReturn(expectedWeathers);
//   }
// }
