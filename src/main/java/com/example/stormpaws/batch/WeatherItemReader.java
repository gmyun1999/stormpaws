// package com.example.stormpaws.batch;

// import com.example.stormpaws.domain.constant.City;
// import java.util.ArrayList;
// import java.util.List;
// import org.springframework.batch.core.StepExecution;
// import org.springframework.batch.core.annotation.BeforeStep;
// import org.springframework.batch.item.ExecutionContext;
// import org.springframework.batch.item.ItemReader;
// import org.springframework.stereotype.Component;

// @Component
// public class WeatherItemReader implements ItemReader<List<City>> {
//   private List<City> cities;
//   private int currentIndex = 0;
//   private static final int BATCH_SIZE = 20;
//   private boolean isCompleted = false;

//   @BeforeStep
//   @SuppressWarnings("unchecked")
//   public void beforeStep(StepExecution stepExecution) {
//     ExecutionContext executionContext = stepExecution.getExecutionContext();
//     Object citiesObj = executionContext.get("cities");
//     if (citiesObj instanceof List<?>) {
//       this.cities = (List<City>) citiesObj;
//     } else {
//       throw new IllegalStateException("Expected List<City> in execution context");
//     }
//     this.currentIndex = 0;
//     this.isCompleted = false;
//   }

//   @Override
//   public List<City> read() {
//     if (isCompleted || currentIndex >= cities.size()) {
//       isCompleted = true;
//       return null;
//     }

//     int endIndex = Math.min(currentIndex + BATCH_SIZE, cities.size());
//     List<City> batch = new ArrayList<>(cities.subList(currentIndex, endIndex));
//     currentIndex = endIndex;

//     if (currentIndex >= cities.size()) {
//       isCompleted = true;
//     }

//     return batch;
//   }
// }
