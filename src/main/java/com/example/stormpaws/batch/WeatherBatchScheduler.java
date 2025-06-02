package com.example.stormpaws.batch;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherBatchScheduler {
  private final JobLauncher jobLauncher;
  private final Job weatherJob;
  private final JdbcTemplate jdbcTemplate;

  @PostConstruct
  public void init() {
    log.info("DB CHECK: 애플리케이션 시작 시 데이터베이스 현황을 확인합니다.");
    try {
      // 전체 데이터 건수 확인
      Integer totalRows =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM weather_log_model", Integer.class);
      log.info("DB CHECK: weather_log_model 테이블의 전체 데이터 건수: {}", totalRows);

    } catch (Exception e) {
      log.error("DB CHECK: 데이터베이스 현황 확인 중 오류 발생: {}", e.getMessage(), e);
    }

    log.info("서버 시작 시 날씨 데이터 배치 작업을 실행합니다.");
    runWeatherBatch();
  }

  @Scheduled(cron = "0 0 12 * * ?") // 매 시간마다 실행
  public void runWeatherBatch() {
    try {
      JobParameters jobParameters =
          new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();

      jobLauncher.run(weatherJob, jobParameters);
      log.info("날씨 데이터 배치 작업이 성공적으로 실행되었습니다.");
    } catch (Exception e) {
      log.error("날씨 데이터 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
    }
  }
}
