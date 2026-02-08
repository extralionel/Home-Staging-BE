package io.home.staging.model.response;

import io.home.staging.entity.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusResponse {
    private Long jobId;
    private String status;
    private String type;
    private String imageUrl;

  public JobStatusResponse(JobStatus job) {
    this.jobId = job.getId();
    this.status = job.getStatus().toString();
    this.type = "IMAGE";
    this.imageUrl = job.getImageUrl();
  }
}
