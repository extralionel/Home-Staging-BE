package io.home.staging.repository;

import io.home.staging.entity.JobStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {

  Optional<JobStatus> findById(Long id);

  default JobStatus findByIdOrElseThrow(Long jobId) {
    return findById(jobId).orElseThrow(
        () -> new EntityNotFoundException("Job not found for id: " + jobId));
  }
}
