package io.home.staging.ai;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Slf4j
@Component
@Profile("mock-image-generator")
public class MockImageGenerator implements ImageGenerator {

  private static final String MOCK_IMAGE_PATH = "image/cozy-room.jpg";

  @Override
  @Async("imageTaskExecutor")
  public CompletableFuture<byte[]> generateImage(String prompt, byte[] image,
      io.home.staging.entity.ImageQuality quality) {
    log.info("Starting to generate mock image...");
    System.out.println("Processing in: " + Thread.currentThread().getName());
    return CompletableFuture.supplyAsync(() -> {
      try {
        ClassPathResource resource = new ClassPathResource(MOCK_IMAGE_PATH);
        if (!resource.exists()) {
          throw new IllegalStateException(
              "Image not found at classpath location: " + MOCK_IMAGE_PATH);
        }

        log.info("Generated mock image...");
        try (InputStream in = resource.getInputStream()) {
          return StreamUtils.copyToByteArray(in);
        }
      } catch (IOException e) {
        throw new CompletionException("Failed to read mock image", e);
      }
    }, CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS));
  }
}
