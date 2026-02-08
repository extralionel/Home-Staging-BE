package io.home.staging.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class WebConfig {

  @Bean(name = "imageProcessorPool")
  public ExecutorService imageProcessorPool() {
    int cores = Runtime.getRuntime().availableProcessors();
    return Executors.newFixedThreadPool(cores);
  }

}
