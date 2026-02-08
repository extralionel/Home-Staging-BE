package io.home.staging.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import io.home.staging.entity.Token;

import org.springframework.transaction.annotation.Transactional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

  @Query("SELECT T FROM Token T WHERE T.user.id = :id")
  List<Token> findAllValidTokenByUser(Integer id);

  Optional<Token> findByToken(String token);

  @Modifying
  @Transactional
  @Query("DELETE FROM Token T WHERE T.expired = true OR T.revoked = true")
  void deleteExpiredOrRevokedTokens();
}
