package io.home.staging.model.request.image;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class GeminiRequest {

  // --- REQUEST ---
  public record Request(List<Content> contents, GenerationConfig generationConfig) {}

  public record Content(String role, List<Part> parts) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Part(String text, InlineData inlineData) {}

  public record InlineData(String mimeType, String data) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record GenerationConfig(
      List<String> responseModalities,
      ImageConfig imageConfig // Nueva configuración de imagen
  ) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ImageConfig(
      String aspectRatio,
      String imageSize // Aquí controlamos la "Calidad": 1K, 2K o 4K
  ) {}

  // --- RESPONSE ---
  public record Response(List<Candidate> candidates) {}

  public record Candidate(Content content) {}

  // --- APP RESPONSE ---
  public record AppImageResponse(String mimeType, String base64Image) {}
}