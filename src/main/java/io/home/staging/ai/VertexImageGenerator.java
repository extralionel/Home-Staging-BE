package io.home.staging.ai;

import com.google.auth.oauth2.GoogleCredentials;
import io.home.staging.entity.ImageQuality;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Profile("!mock-image-generator")
public class VertexImageGenerator implements ImageGenerator {

  @Value("${gemini.project.id}")
  private String projectId;

  @Value("${gemini.location:us-central1}")
  private String location;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public VertexImageGenerator() {
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
  }

  @Async("imageTaskExecutor")
  @Override
  public CompletableFuture<byte[]> generateImage(String prompt, byte[] image,
      ImageQuality quality) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        log.info("Starting image generation with quality: {}", quality);

        // 1. Initial Generation
        byte[] generatedImage = callGenerationApi(prompt, image);

        // 2. Upscaling if needed
        if (quality == ImageQuality.HD_2K) {
          log.info("Upscaling to 2K (x2)...");
          return callUpscaleApi(generatedImage, "x2");
        } else if (quality == ImageQuality.UHD_4K) {
          log.info("Upscaling to 4K (x4)...");
          return callUpscaleApi(generatedImage, "x4");
        }

        return generatedImage;

      } catch (Exception e) {
        log.error("Image generation failed", e);
        throw new RuntimeException("Image generation failed", e);
      }
    });
  }

  private byte[] callGenerationApi(String prompt, byte[] inputImage) throws IOException {
    String url = String.format(
        "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/image-3.0-generate-001:predict",
        location, projectId, location);

    String base64Image = Base64.getEncoder().encodeToString(inputImage);

    // Vertex AI Prediction Payload
    Map<String, Object> instance = Map.of(
        "prompt", prompt,
        "image", Map.of("bytesBase64Encoded", base64Image));

    Map<String, Object> parameters = Map.of(
        "sampleCount", 1,
        "aspectRatio", "1:1" // Or make this configurable if needed
    );

    Map<String, Object> body = Map.of(
        "instances", List.of(instance),
        "parameters", parameters);

    return executeRequest(url, body);
  }

  private byte[] callUpscaleApi(byte[] inputImage, String upscaleConfig) throws IOException {
    String url = String.format(
        "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/image-3.0-generate-001:predict",
        location, projectId, location);

    String base64Image = Base64.getEncoder().encodeToString(inputImage);

    Map<String, Object> instance = Map.of(
        "image", Map.of("bytesBase64Encoded", base64Image));

    Map<String, Object> parameters = Map.of(
        "upscaleConfig", Map.of("upscaleFactor", upscaleConfig));

    Map<String, Object> body = Map.of(
        "instances", List.of(instance),
        "parameters", parameters);

    return executeRequest(url, body);
  }

  private byte[] executeRequest(String url, Map<String, Object> body) throws IOException {
    String accessToken = getAccessToken();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(accessToken);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      return extractImageFromResponse(response.getBody());
    } else {
      throw new RuntimeException("Vertex AI API call failed: " + response.getStatusCode());
    }
  }

  private String getAccessToken() throws IOException {
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
    credentials.refreshIfExpired();
    return credentials.getAccessToken().getTokenValue();
  }

  private byte[] extractImageFromResponse(String jsonResponse) throws IOException {
    JsonNode root = objectMapper.readTree(jsonResponse);
    JsonNode predictions = root.path("predictions");

    if (predictions.isArray() && !predictions.isEmpty()) {
      JsonNode firstPrediction = predictions.get(0);
      String base64String = firstPrediction.path("bytesBase64Encoded").asText();

      if (base64String == null || base64String.isEmpty()) {
        // Sometimes it might be directly in 'image' field inside prediction depending
        // on model version
        base64String = firstPrediction.path("image").path("bytesBase64Encoded").asText();
      }

      if (base64String != null && !base64String.isEmpty()) {
        return Base64.getDecoder().decode(base64String);
      }
    }

    throw new RuntimeException("No image data found in Vertex AI response");
  }
}
