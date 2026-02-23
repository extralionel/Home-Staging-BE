package io.home.staging.ai;

import io.home.staging.api.GoogleApiClient;
import io.home.staging.entity.ImageQuality;
import io.home.staging.model.request.image.GeminiRequest;
import io.home.staging.model.request.image.GeminiRequest.Candidate;
import io.home.staging.model.request.image.GeminiRequest.Content;
import io.home.staging.model.request.image.GeminiRequest.GenerationConfig;
import io.home.staging.model.request.image.GeminiRequest.ImageConfig;
import io.home.staging.model.request.image.GeminiRequest.InlineData;
import io.home.staging.model.request.image.GeminiRequest.Part;
import io.home.staging.model.request.image.GeminiRequest.Request;
import io.home.staging.model.request.image.GeminiRequest.Response;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@Profile("!mock-image-generator")
public class VertexImageGenerator implements ImageGenerator {

  private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/heic");
  private static final Set<String> ALLOWED_RATIOS = Set.of("1:1", "2:3", "3:2", "3:4", "4:3", "4:5", "5:4", "9:16", "16:9", "21:9");
  private static final Set<String> ALLOWED_QUALITIES = Set.of("1K", "2K", "4K");

  private final GoogleApiClient googleApiClient;

  public VertexImageGenerator(GoogleApiClient googleApiClient) {
    this.googleApiClient = googleApiClient;
  }

  @Override
  @Async("imageTaskExecutor")
  public CompletableFuture<byte[]> generateImage(String prompt, MultipartFile imageFile, ImageQuality quality) {
    try {
      String mimeType = imageFile.getContentType();
      String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());

      Part promptPart = new Part(prompt, null);
      Part imagePart = new Part(null, new InlineData(mimeType, base64Image));
      Content content = new Content("user", List.of(promptPart, imagePart));


      // 3. Configurar Calidad (Size) y Aspect Ratio
      ImageConfig imageConfig = new ImageConfig("16:9", "1K");
      GenerationConfig config = new GenerationConfig(List.of("IMAGE"), imageConfig);

      // 4. Armar el Request final
      Request request = new Request(List.of(content), config);

      log.info("Generating image 1K and 16:9...");

      Response response = googleApiClient.generateImage(request);
      Candidate candidate = response.candidates().get(0);
      InlineData data = candidate.content().parts().get(0).inlineData();

      return CompletableFuture.completedFuture(Base64.getDecoder().decode(data.data()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
