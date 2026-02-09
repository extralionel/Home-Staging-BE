package io.home.staging.controller;

import io.home.staging.model.request.PromptRequest;
import io.home.staging.model.response.JobStatusResponse;
import io.home.staging.service.JobService;
import io.home.staging.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
  public ResponseEntity<JobStatusResponse> generatePrompt(
      @RequestPart("prompt") PromptRequest request,
      @RequestPart("file") MultipartFile file,
      Authentication authentication) {
    JobStatusResponse jobStatus = projectService.initiateGeneration(request, file, authentication);
    return ResponseEntity.ok(jobStatus);
  }

  @GetMapping("/status/{jobId}")
  public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable Long jobId) {
    return ResponseEntity.ok(jobService.getJobStatus(jobId));
  }

  @GetMapping
  public ResponseEntity<Void> getProjects(Authentication authentication) {
    return ResponseEntity.ok().build();
  }

}
