package io.home.staging.service;

import io.home.staging.model.request.UserProfileRequest;
import io.home.staging.model.response.UserResponse;
import io.home.staging.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserResponse getUserProfile(Authentication authentication) {
    String email = authentication.getName();
    return new UserResponse(userRepository.findByEmailOrThrow(email));
  }
}
