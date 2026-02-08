package io.home.staging.service;

import io.home.staging.entity.Image;
import io.home.staging.entity.JobStatus;
import io.home.staging.entity.JobStatus.Status;
import io.home.staging.entity.User;
import io.home.staging.model.request.PromptRequest;
import io.home.staging.model.response.ImageResponse;
import io.home.staging.model.response.JobStatusResponse;
import io.home.staging.repository.ImageRepository;
import io.home.staging.repository.JobStatusRepository;
import io.home.staging.repository.UserRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PromptService {

  private final UserRepository userRepository;
  private final ImageService imageService;
  private final ImageRepository imageRepository;
  private final VertexService vertexService;
  private final JobService jobService;

  public PromptService(
      UserRepository userRepository,
      ImageService imageService,
      ImageRepository imageRepository,
      VertexService vertexService,

      JobService jobService) {
    this.userRepository = userRepository;
    this.imageService = imageService;
    this.imageRepository = imageRepository;
    this.vertexService = vertexService;
    this.jobService = jobService;
  }

  public JobStatusResponse initiateGeneration(PromptRequest request, MultipartFile file,
      Authentication authentication) {
    String name = authentication.getName();
    User user = userRepository.findByEmailOrThrow(name);
    JobStatusResponse job = jobService.createJob(Status.PENDING, user);

    // TODO: Validate if user has credits

    try {
      // We need to keep the file bytes as the file might be cleared after the request
      // finishes
      byte[] fileBytes = file.getBytes();

      Image originalImage = imageService.uploadImage(authentication, file);
      log.info("Uploaded reference image: {}", originalImage.getUrl());

      String promptText = String.format(
          "Update the attached image (home-staging) and transform this %s into a %s style. %s. Final output should be high-quality and realistic.",
          request.getRoomType(),
          request.getStyle(),
          request.getAdditionalDetails() != null ? request.getAdditionalDetails() : "");

      CompletableFuture<byte[]> generatedImageBytes = vertexService.generateModifiedImage(fileBytes, promptText);

      // 4. Upload generated image
      Image generatedImage = imageService.uploadImage(authentication, generatedImageBytes);

      log.info("Successfully completed job {}: {}", job.getJobId(), generatedImage.getUrl());
      return job;
    } catch (Exception e) {
      jobService.updateJobStatus(job.getJobId(), Status.FAILED);
      throw new RuntimeException(e);
    }
  }
}
