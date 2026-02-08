package io.home.staging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeminiWebConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
