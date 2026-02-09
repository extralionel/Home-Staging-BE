package io.home.staging.model.response;

import io.home.staging.entity.Image;
import io.home.staging.entity.Project;
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
  private List<String> imageUrls;

  public ProjectResponse(Project project) {
    this.projectId = project.getId();
    this.imageUrls = project.getImages().stream()
        .map(Image::getUrl)
        .collect(Collectors.toList());
  }
}
