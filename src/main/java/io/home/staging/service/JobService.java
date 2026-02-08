package io.home.staging.service;

import io.home.staging.entity.JobStatus;
import io.home.staging.entity.JobStatus.Status;
import io.home.staging.entity.User;
import io.home.staging.model.response.JobStatusResponse;
import io.home.staging.repository.JobStatusRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobService {
  private final JobStatusRepository jobStatusRepository;

  public JobService(JobStatusRepository jobStatusRepository) {
    this.jobStatusRepository = jobStatusRepository;
  }

  public JobStatusResponse createJob(Status status, User user) {
    JobStatus job = new JobStatus();
    job.setCreatedAt(LocalDateTime.now());
    job.setUser(user);
    job.setStatus(status);

    job = jobStatusRepository.save(job);
    return new JobStatusResponse(job);
  }

  public void updateJobStatus(Long jobId, Status status) {
    JobStatus job = jobStatusRepository.findByIdOrElseThrow(jobId);
    job.setStatus(status);
    jobStatusRepository.save(job);
  }

  public JobStatusResponse getJobStatus(Long jobId) {
    JobStatus job = jobStatusRepository.findByIdOrElseThrow(jobId);
    return new JobStatusResponse(job);
  }
}
