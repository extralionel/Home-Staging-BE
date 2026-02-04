package io.home.staging.service;

import io.home.staging.entity.User;
import io.home.staging.model.request.PromptRequest;
import io.home.staging.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PromptService {

  private final UserRepository userRepository;
  private final ImageService imageService;

  public PromptService(UserRepository userRepository, ImageService imageService) {
    this.userRepository = userRepository;
    this.imageService = imageService;
  }

  public void generate(PromptRequest request, MultipartFile file, Authentication authentication) {
    String name = authentication.getName();
    User user = userRepository.findByEmailOrThrow(name);
    if (file != null && !file.isEmpty()) {
      imageService.uploadImage(authentication, file);
      log.info("Uploaded and linked image for generation request");
    }
    log.info("Generating prompt for user {}", name);
  }
}
