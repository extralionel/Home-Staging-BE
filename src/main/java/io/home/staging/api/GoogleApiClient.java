package io.home.staging.api;

import io.home.staging.model.request.image.GeminiRequest.Request;
import io.home.staging.model.request.image.GeminiRequest.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleApiClient {

  private final RestClient googleApiClient;
  private final String apiKey;
  private final String model;


  public GoogleApiClient(
      @Qualifier("geminiClient") RestClient googleApiClient,
      @Value("${google.gemini.api.key}") String apiKey,
      @Value("${google.gemini.model}") String model
  ) {
    this.googleApiClient = googleApiClient;
    this.apiKey = apiKey;
    this.model = model;
  }

  public Response generateImage(Request request) {
    return googleApiClient.post()
        .uri(uriBuilder -> uriBuilder
            .pathSegment(model + ":generateContent")
            .queryParam("key", apiKey)
            .build())
        .body(request)
        .retrieve()
        .body(Response.class);
  }

  public <T> T execute(HttpMethod method, Object payload, Class<T> clazz) {
    return googleApiClient.method(method)
        .uri(uriBuilder -> uriBuilder
            .pathSegment(model + ".generateContent")
            .queryParam("key", apiKey)
            .build())
        .body(payload)
        .retrieve()
        .body(clazz);
  }

}
