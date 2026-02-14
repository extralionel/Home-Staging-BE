package io.home.staging.ai;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

public interface ImageGenerator {

  @Async("imageTaskExecutor")
  CompletableFuture<byte[]> generateImage(String prompt, byte[] image, io.home.staging.entity.ImageQuality quality);
}
