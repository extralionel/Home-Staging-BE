package io.home.staging.repository;

import io.home.staging.entity.Plan;
import io.home.staging.entity.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    Optional<Plan> findByType(PlanType type);
}
