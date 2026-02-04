package io.home.staging.repository;

import io.home.staging.entity.Image;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
  Optional<Image> findByPublicId(String publicId);

  default Image findByPublicIdOrThrow(String publicId) {
    return findByPublicId(publicId).orElseThrow(
        () -> new EntityNotFoundException("Image not found for public id " + publicId));
  }
}
