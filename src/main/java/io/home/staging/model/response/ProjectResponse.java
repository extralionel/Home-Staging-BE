package io.home.staging.model.response;

import io.home.staging.entity.Image;
import io.home.staging.entity.JobStatus;
import io.home.staging.entity.Project;
import java.time.Instant;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
  private Long projectId;
  private String prompt;
  private List<String> imageUrls;
  private JobStatusResponse jobStatus;
  private Instant createdAt;

  public ProjectResponse(Project project) {
    this.projectId = project.getId();
    this.imageUrls = project.getImages().stream()
        .map(Image::getUrl)
        .collect(Collectors.toList());
    this.jobStatus = new JobStatusResponse(project.getJob());
    this.prompt = project.getPrompt();
    this.createdAt = project.getCreatedAt();
  }
}
