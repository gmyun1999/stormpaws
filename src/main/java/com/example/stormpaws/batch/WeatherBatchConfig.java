package com.example.stormpaws.batch;

import com.example.stormpaws.domain.constant.City;
import com.example.stormpaws.service.dto.CityWeatherInfoDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class WeatherBatchConfig {

  @Bean
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2); // worker 수를 2로 변경
    executor.setMaxPoolSize(2); // worker 수를 2로 변경
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("weather-batch-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }

  @Bean
  @Qualifier("masterStep")
  public Step weatherStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      WeatherItemReader reader,
      WeatherItemProcessor processor,
      WeatherItemWriter writer,
      @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
    return new StepBuilder("weatherStep", jobRepository)
        .partitioner("workerStep", partitioner())
        .step(
            workerStep(jobRepository, transactionManager, reader, processor, writer, taskExecutor))
        .build();
  }

  @Bean
  @Qualifier("workerStep")
  public Step workerStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      WeatherItemReader reader,
      WeatherItemProcessor processor,
      WeatherItemWriter writer,
      @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
    return new StepBuilder("workerStep", jobRepository)
        .<List<City>, List<CityWeatherInfoDTO>>chunk(20, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .taskExecutor(taskExecutor)
        .build();
  }

  @Bean
  public Job weatherJob(JobRepository jobRepository, @Qualifier("masterStep") Step weatherStep) {
    return new JobBuilder("weatherJob", jobRepository).start(weatherStep).build();
  }

  @Bean
  public Partitioner partitioner() {
    return new SimplePartitioner() {
      @Override
      public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();

        // 도시 목록을 2개의 파티션으로 나눔
        List<City> cities = new ArrayList<>(Arrays.asList(City.values()));
        int partitionSize = (int) Math.ceil(cities.size() / 2.0);

        for (int i = 0; i < 2; i++) {
          ExecutionContext context = new ExecutionContext();
          int start = i * partitionSize;
          int end = Math.min(start + partitionSize, cities.size());

          List<City> partitionCities = new ArrayList<>(cities.subList(start, end));
          context.put("cities", partitionCities);
          result.put("partition" + i, context);
        }

        return result;
      }
    };
  }
}
