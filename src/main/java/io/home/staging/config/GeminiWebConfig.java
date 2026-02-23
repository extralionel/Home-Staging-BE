package io.home.staging.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiWebConfig {

  private final String baseUrl;

  public GeminiWebConfig(@Value("${google.gemini.api.url}") String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @Bean("geminiClient")
  public RestClient geminiClient(RestClient.Builder builder) {
    return builder
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
