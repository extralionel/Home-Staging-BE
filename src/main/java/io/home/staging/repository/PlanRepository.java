package io.home.staging.repository;

import io.home.staging.entity.Plan;
import io.home.staging.entity.PlanType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {
    Optional<Plan> findByType(PlanType type);

    default Plan findByPlanTypeOrThrow(PlanType type) {
      return findByType(type).orElseThrow(
          () -> new EntityNotFoundException("No plan with type " + type));
    }
}
