package io.home.staging.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class WebConfig {

  @Bean(name = "imageTaskExecutor")
  public Executor imageProcessorPool() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);       // minimum number of threads
    executor.setMaxPoolSize(50);        // max threads allowed
    executor.setQueueCapacity(100);     // queue size before new threads
    executor.setThreadNamePrefix("image-thread-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    executor.initialize();
    return executor;
  }

}
