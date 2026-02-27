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

  @org.springframework.data.jpa.repository.Modifying
  @org.springframework.data.jpa.repository.Query("UPDATE JobStatus j SET j.status = :targetStatus, j.updatedAt = CURRENT_TIMESTAMP WHERE j.status = :currentStatus AND j.updatedAt < :threshold")
  int markStaleJobsAsFailed(
      @org.springframework.data.repository.query.Param("currentStatus") JobStatus.Status currentStatus,
      @org.springframework.data.repository.query.Param("targetStatus") JobStatus.Status targetStatus,
      @org.springframework.data.repository.query.Param("threshold") java.time.LocalDateTime threshold);
}
