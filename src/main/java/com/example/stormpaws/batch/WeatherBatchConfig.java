package com.example.stormpaws.batch;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class WeatherBatchConfig {

  @Bean
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("weather-batch-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }

  @Bean
  public Job weatherJob(JobRepository jobRepository, Step weatherStep) {
    return new JobBuilder("weatherJob", jobRepository).start(weatherStep).build();
  }

  @Bean
  public Step weatherStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      WeatherItemReader reader,
      WeatherItemProcessor processor,
      WeatherItemWriter writer,
      @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
    return new StepBuilder("weatherStep", jobRepository)
        .<List<City>, List<CityWeatherInfoDTO>>chunk(5, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .taskExecutor(taskExecutor)
        .throttleLimit(5)
        .build();
  }
}
