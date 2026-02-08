package io.home.staging.service;

import io.home.staging.entity.Image;
import io.home.staging.model.response.GeminiResponseWrapper;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class VertexService {

  @Value("${gemini.api.key}")
  private String apiKey;

  private final RestTemplate restTemplate;

  public VertexService(final RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Async
  public CompletableFuture<byte[]> generateModifiedImage(byte[] inputImageBytes, String prompt) {
    log.info("Generating modified image via REST API...");

    try {
      String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-image-preview:generateContent?key="
          + apiKey;

      String base64Image = Base64.getEncoder().encodeToString(inputImageBytes);

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
        return extractImageFromResponse(response.getBody());
      }

      throw new RuntimeException("API call failed with status: " + response.getStatusCode());
    } catch (Exception e) {
      log.error("Error generating image via REST API", e);
      throw new RuntimeException("Failed to generate image", e);
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
