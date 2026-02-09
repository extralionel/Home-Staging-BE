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

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;

  public ProjectService(
      ImageService imageService,
      ImageGenerator imageGenerator,
      JobService jobService,
      UserRepository userRepository,
      ProjectRepository projectRepository
  ) {
    this.imageService = imageService;
    this.jobService = jobService;
    this.imageGenerator = imageGenerator;
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

  public JobStatusResponse initiateGeneration(PromptRequest request, MultipartFile file,
      Authentication authentication) {
    String name = authentication.getName();
    User user = userRepository.findByEmailOrThrow(name);

    JobStatus job = jobService.createJob(Status.PENDING, user);
    Project project = projectRepository.save(new Project(user, job));

    // TODO: Validate if user has credits

    try {
      // We need to keep the file bytes as the file might be cleared after the request
      // finishes
      byte[] fileBytes = file.getBytes();

      String promptText = String.format(
          "Update the attached image (home-staging) and transform this %s into a %s style. %s. Final output should be high-quality and realistic.",
          request.getRoomType(),
          request.getStyle(),
          request.getAdditionalDetails() != null ? request.getAdditionalDetails() : "");

      CompletableFuture<byte[]> imageBytesFuture = imageGenerator.generateImage(promptText, fileBytes);

      imageBytesFuture.thenAccept(imageBytes -> {
        jobService.updateJobStatus(job.getId(), Status.PROCESSING);

        // Create new project
        Image image = imageService.uploadImage(authentication, project, imageBytes);
        jobService.completeJob(job.getId());
        log.info("Successfully completed job {}: {}", job.getId(), image.getUrl());
      });

      log.info("Processing job {}: {}", job.getId(), promptText);
      return new JobStatusResponse(job);
    } catch (Exception e) {
      jobService.updateJobStatus(job.getId(), Status.FAILED);
      throw new RuntimeException(e);
    }
  }
}
