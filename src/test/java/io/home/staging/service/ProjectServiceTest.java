package io.home.staging.service;

import io.home.staging.repository.ImageRepository;
import io.home.staging.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private ImageService imageService;
  @Mock private ImageRepository imageRepository;
  @Mock private JobService jobService;

  @Mock private Authentication authentication;
  @Mock private MultipartFile file;

  @InjectMocks
  private ProjectService projectService;

}