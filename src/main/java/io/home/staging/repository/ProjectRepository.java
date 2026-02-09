package io.home.staging.repository;

import io.home.staging.entity.Project;
import io.home.staging.entity.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  default Project findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(
        () -> new EntityNotFoundException("Project with id: " + id + " not found."));
  }

  @Query("SELECT P FROM Project P WHERE P.user = :user")
  List<Project> findAllByUser(User user);
}
