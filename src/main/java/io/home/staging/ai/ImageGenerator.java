package io.home.staging.ai;

import io.home.staging.entity.ImageQuality;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

public interface ImageGenerator {

  @Async("imageTaskExecutor")
  CompletableFuture<byte[]> generateImage(String prompt, MultipartFile image, ImageQuality quality);
}
