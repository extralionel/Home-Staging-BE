package io.home.staging.service;

import io.home.staging.ai.ImageGenerator;
import io.home.staging.entity.Image;
import io.home.staging.entity.JobStatus;
import io.home.staging.entity.JobStatus.Status;
import io.home.staging.entity.Project;
import io.home.staging.entity.User;
import io.home.staging.model.request.PromptRequest;
import io.home.staging.model.response.JobStatusResponse;
import io.home.staging.model.response.ProjectResponse;
import io.home.staging.repository.ProjectRepository;
import io.home.staging.repository.UserRepository;
import io.home.staging.validator.CreditsValidator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ProjectService {

  private final ImageService imageService;
  private final ImageGenerator imageGenerator;
  private final JobService jobService;

  private final CreditsValidator creditsValidator;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;

  public ProjectService(
      ImageService imageService,
      ImageGenerator imageGenerator,
      JobService jobService,
      CreditsValidator creditsValidator,
      UserRepository userRepository,
      ProjectRepository projectRepository
  ) {
    this.imageService = imageService;
    this.jobService = jobService;
    this.imageGenerator = imageGenerator;
    this.creditsValidator = creditsValidator;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
  }

  public List<ProjectResponse> getProjects(Authentication authentication) {
    String name = authentication.getName();
    User user = userRepository.findByEmailOrThrow(name);

    return projectRepository.findAllByUser(user).stream()
        .map(ProjectResponse::new)
        .collect(Collectors.toList());
  }

  public JobStatusResponse initiateGeneration(PromptRequest request, MultipartFile file, User user) {
    creditsValidator.validate(user, request);
    JobStatus job = jobService.initJob(Status.PENDING, user);

    try {
      String promptText = String.format(
          "Photorealistic virtual staging of a %s, decorated in a premium %s style. " +
              "**OBJECTIVE:** Furnish the space with high-quality furniture, rugs, and decor that match the room's scale and perspective."
              +
              "**CONSTRAINTS:** Strictly preserve the original structural integrity, including wall positions, window views, ceiling details, and existing flooring materials. Do not alter architectural features. "
              +
              "**INTEGRATION:** Ensure new furniture casts realistic shadows on the floor and interacts naturally with the existing lighting direction. "
              +
              "**AESTHETIC:** Clean lines, decluttered, magazine-quality composition, 8k resolution." +
              "%s",
          request.getRoomType(),
          request.getStyle(),
          request.getAdditionalDetails() != null ? "Specific details: " + request.getAdditionalDetails() : "");

      Project project = projectRepository.save(new Project(user, job, promptText));
      CompletableFuture<byte[]> imageBytesFuture = imageGenerator.generateImage(promptText,
          file, request.getQuality());

      imageBytesFuture.thenAccept(imageBytes -> {
        jobService.updateJobStatus(job.getId(), Status.PROCESSING);

        // Create new project
        Image image = imageService.uploadImage(user, project, imageBytes);
        jobService.completeJob(job.getId());
        log.info("[SUCCESS] - Completed jobId {}. imageUrl: {}", job.getId(), image.getUrl());
      });

      log.info("[PROGRESS] - Starting image generation jobId: {}", job.getId());
      return new JobStatusResponse(job);
    } catch (Exception e) {
      jobService.updateJobStatus(job.getId(), Status.FAILED);
      throw new RuntimeException(e);
    }
  }
}
