package io.home.staging.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponseWrapper {

  private GeminiBody body;

  public record GeminiBody(List<Candidate> candidates) {
  }

  public record Candidate(Content content) {
  }

  public record Content(List<Part> parts) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Part(
      String text,
      InlineData inlineData) {
  }

  public record InlineData(String mimeType, String data) {
  }

}
