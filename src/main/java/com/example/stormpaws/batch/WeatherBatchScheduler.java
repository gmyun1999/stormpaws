// package com.example.stormpaws.batch;

// import jakarta.annotation.PostConstruct;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.batch.core.Job;
// import org.springframework.batch.core.JobParameters;
// import org.springframework.batch.core.JobParametersBuilder;
// import org.springframework.batch.core.launch.JobLauncher;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class WeatherBatchScheduler {
//   private final JobLauncher jobLauncher;
//   private final Job weatherJob;

//   @PostConstruct
//   public void init() {
//     runWeatherBatch();
//   }

//   @Scheduled(cron = "0 0 12 * * ?")
//   public void runWeatherBatch() {
//     try {
//       JobParameters jobParameters =
//           new JobParametersBuilder().addLong("time",
// System.currentTimeMillis()).toJobParameters();
//       jobLauncher.run(weatherJob, jobParameters);
//     } catch (Exception e) {
//       log.error("error: {}", e.getMessage(), e);
//     }
//   }
// }
