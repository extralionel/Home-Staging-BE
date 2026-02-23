package io.home.staging.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class MailgunWebConfig {

  private final String domain;
  private final String apiKey;
  private final String baseUrl;

  public MailgunWebConfig(
      @Value("${email.mailgun.base-url}") String baseUrl,
      @Value("${email.mailgun.domain}") String domain,
      @Value("${email.mailgun.api-key}") String apiKey) {
    this.baseUrl = baseUrl;
    this.domain = domain;
    this.apiKey = apiKey;
  }

  @Bean("mailgunClient")
  public RestClient mailgunClient(RestClient.Builder builder) {
    String auth = "api:" + apiKey;
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    String authHeader = "Basic " + encodedAuth;

    return builder
        .baseUrl(baseUrl + domain)
        .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
        .build();
  }
}
