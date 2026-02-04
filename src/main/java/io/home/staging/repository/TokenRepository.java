package io.home.staging.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import io.home.staging.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query("SELECT T FROM Token T WHERE T.user.id = :id")
    List<Token> findAllValidTokenByUser(Integer id);

    Optional<Token> findByToken(String token);
}
