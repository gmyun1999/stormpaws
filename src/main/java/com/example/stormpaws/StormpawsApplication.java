package com.example.stormpaws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StormpawsApplication {

  public static void main(String[] args) {
    SpringApplication.run(StormpawsApplication.class, args);
  }
}
