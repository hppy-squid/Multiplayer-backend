package com.multiplayer_grupp1.multiplayer_grupp1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {
  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    var s = new ThreadPoolTaskScheduler();
    s.setPoolSize(2);
    s.setThreadNamePrefix("quiz-");
    return s;
  }
}
