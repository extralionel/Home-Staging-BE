package io.home.staging.controller;

import io.home.staging.model.request.PromptRequest;
import io.home.staging.model.response.JobStatusResponse;
import io.home.staging.service.PromptService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
public class PromptController {

  private final PromptService promptService;

  @Autowired
  public PromptController(PromptService promptService) {
    this.promptService = promptService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<JobStatusResponse> generatePrompt(
      @RequestPart("prompt") PromptRequest request,
      @RequestPart("file") MultipartFile file,
      Authentication authentication) {
    JobStatusResponse jobStatus = promptService.initiateGeneration(request, file, authentication);
    return ResponseEntity.ok(jobStatus);
  }

  @GetMapping("/status/{jobId}")
  public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable Long jobId) {
    return ResponseEntity.ok(promptService.getJobStatus(jobId));
  }

}
