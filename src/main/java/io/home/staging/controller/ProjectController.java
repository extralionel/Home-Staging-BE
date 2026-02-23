package io.home.staging.controller;

import io.home.staging.entity.User;
import io.home.staging.model.request.PromptRequest;
import io.home.staging.model.response.JobStatusResponse;
import io.home.staging.model.response.ProjectResponse;
import io.home.staging.service.JobService;
import io.home.staging.service.ProjectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/prompt")
public class ProjectController {

  private final ProjectService projectService;
  private final JobService jobService;

  @Autowired
  public ProjectController(ProjectService projectService, JobService jobService) {
    this.projectService = projectService;
    this.jobService = jobService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<JobStatusResponse> generateProject(
      @RequestPart("prompt") PromptRequest request,
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal User user
  ) {
    JobStatusResponse jobStatus = projectService.initiateGeneration(request, file, user);
    return ResponseEntity.ok(jobStatus);
  }

  @GetMapping("/projects")
  public ResponseEntity<List<ProjectResponse>> getProjects(Authentication authentication) {
    return ResponseEntity.ok(projectService.getProjects(authentication));
  }

  @GetMapping("/status/{jobId}")
  public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable Long jobId) {
    return ResponseEntity.ok(jobService.getJobStatus(jobId));
  }
}
