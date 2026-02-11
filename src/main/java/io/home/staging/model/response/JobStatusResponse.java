package io.home.staging.model.response;

import io.home.staging.entity.JobStatus;
import io.home.staging.entity.JobStatus.Status;
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
    private Status status;
    private String type;

  public JobStatusResponse(JobStatus job) {
    this.jobId = job.getId();
    this.status = job.getStatus();
    this.type = "IMAGE";
  }
}
