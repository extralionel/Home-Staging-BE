package io.home.staging.ai;

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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Profile("!mock-image-generator")
public class VertexImageGenerator implements ImageGenerator {

  @Value("${gemini.api.key}")
  private String apiKey;

  private final RestTemplate restTemplate;

  public VertexImageGenerator(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Async("imageTaskExecutor")
  @Override
  public CompletableFuture<byte[]> generateImage(String prompt, byte[] image) {
    log.info("Generating modified image via REST API...");

    try {
      String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-image-preview:generateContent?key="
          + apiKey;

      String base64Image = Base64.getEncoder().encodeToString(image);

      // Construct Multimodal Payload
      Map<String, Object> partText = Map.of("text", prompt);
      Map<String, Object> partImage = Map.of(
          "inlineData", Map.of(
              "mimeType", "image/jpeg",
              "data", base64Image));

      Map<String, Object> content = Map.of("parts", List.of(partText, partImage));
      Map<String, Object> body = Map.of("contents", List.of(content));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      log.info("Sending request to Gemini 3 Pro REST API...");
      ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity,
          String.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        log.info("Received successful response from API.");
        return CompletableFuture.completedFuture(extractImageFromResponse(response.getBody()));
      }

      throw new RuntimeException("API call failed with status: " + response.getStatusCode());
    } catch (Exception e) {
      log.error("Error generating image via REST API", e);
      return CompletableFuture.failedFuture(e);
    }
  }

  private byte[] extractImageFromResponse(String payload) {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(payload);

    // 2. Traverse the path: body -> candidates[0] -> content -> parts[0] -> inlineData -> data
    // Note: Adjust "body" depending on if your HTTP client wrapped the response or if it's raw from Google.
    // If raw from Google, start at "candidates".
    JsonNode dataNode = rootNode
        //.path("body") // Remove this line if your JSON starts with 'candidates'
        .path("candidates").get(0)
        .path("content")
        .path("parts").get(0)
        .path("inlineData")
        .path("data");

    if (dataNode.isMissingNode()) {
      throw new RuntimeException("Image data not found in JSON response");
    }

    String base64String = dataNode.asText();

    // 3. Decode Base64 to Bytes
    return Base64.getDecoder().decode(base64String);
  }
}
