package io.home.staging.repository;

import io.home.staging.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT U FROM User U WHERE U.email = :email")
    Optional<User> findByEmail(String email);

    default User findByEmailOrThrow(String email) {
      return findByEmail(email)
          .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Modifying
    @Transactional
    @Query("UPDATE User U SET U.generationsLeft = U.generationsLeft - 1 WHERE U.id = :userId")
    void decrementDailyGenerations(Integer userId);
}
