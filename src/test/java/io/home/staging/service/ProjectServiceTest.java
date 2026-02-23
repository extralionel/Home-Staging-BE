package io.home.staging.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.home.staging.ai.ImageGenerator;
import io.home.staging.entity.Image;
import io.home.staging.entity.ImageQuality;
import io.home.staging.entity.JobStatus;
import io.home.staging.entity.JobStatus.Status;
import io.home.staging.entity.Project;
import io.home.staging.entity.User;
import io.home.staging.exception.ErrorCode;
import io.home.staging.exception.InsufficientCreditsException;
import io.home.staging.model.request.PromptRequest;
import io.home.staging.model.response.JobStatusResponse;
import io.home.staging.model.response.ProjectResponse;
import io.home.staging.repository.ImageRepository;
import io.home.staging.repository.ProjectRepository;
import io.home.staging.repository.UserRepository;
import io.home.staging.validator.CreditsValidator;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
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
  @Mock private JobService jobService;
  @Mock private ProjectRepository projectRepository;
  @Mock private ImageGenerator imageGenerator;
  @Mock private CreditsValidator creditsValidator;

  @Mock private Authentication authentication;
  @Mock private MultipartFile file;

  @InjectMocks
  private ProjectService projectService;

  @Test
  void testGetProjects() {
    User user = new User();
    user.setId(1L);

    Project project = new Project();
    project.setId(10L);
    project.setJobStatus(new JobStatus());

    when(authentication.getName()).thenReturn("test@test.com");
    when(userRepository.findByEmailOrThrow("test@test.com")).thenReturn(user);
    when(projectRepository.findAllByUser(user)).thenReturn(Collections.singletonList(project));

    List<ProjectResponse> projects = projectService.getProjects(authentication);

    assertEquals(1, projects.size());
    assertEquals(10L, projects.get(0).getId());
  }

  @Test
  void testInitiateGeneration_Success() {
    User user = new User();
    user.setId(1L);

    PromptRequest request = new PromptRequest();
    request.setRoomType("Living Room");
    request.setStyle("Modern");
    request.setQuality(ImageQuality.STANDARD);

    JobStatus jobStatus = new JobStatus();
    jobStatus.setId(100L);

    Project project = new Project();
    project.setId(10L);

    when(jobService.initJob(Status.PENDING, user)).thenReturn(jobStatus);
    when(projectRepository.save(any(Project.class))).thenReturn(project);
    
    CompletableFuture<byte[]> futureBytes = CompletableFuture.completedFuture(new byte[]{1, 2, 3});
    when(imageGenerator.generateImage(anyString(), eq(file), eq(ImageQuality.STANDARD))).thenReturn(futureBytes);
    
    Image generatedImage = new Image();
    generatedImage.setUrl("http://example.com/image.jpg");
    when(imageService.uploadImage(eq(user), eq(project), any())).thenReturn(generatedImage);

    JobStatusResponse response = projectService.initiateGeneration(request, file, user);

    assertNotNull(response);
    assertEquals(100L, response.getId());
    
    verify(creditsValidator).validate(user, request);
    verify(jobService).updateJobStatus(100L, Status.PROCESSING);
    verify(jobService).completeJob(100L);
  }

  @Test
  void testInitiateGeneration_ValidationFails() {
    User user = new User();
    PromptRequest request = new PromptRequest();

    doThrow(new InsufficientCreditsException("Insuf", ErrorCode.INSUFFICIENT_CREDITS))
        .when(creditsValidator).validate(user, request);

    assertThrows(InsufficientCreditsException.class, () -> {
      projectService.initiateGeneration(request, file, user);
    });

    verify(jobService, never()).initJob(any(), any());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void testInitiateGeneration_ExceptionDuringProcess() {
    User user = new User();
    PromptRequest request = new PromptRequest();
    request.setRoomType("Living Room");
    request.setStyle("Modern");

    JobStatus jobStatus = new JobStatus();
    jobStatus.setId(100L);

    when(jobService.initJob(Status.PENDING, user)).thenReturn(jobStatus);
    when(projectRepository.save(any(Project.class))).thenThrow(new RuntimeException("DB error"));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      projectService.initiateGeneration(request, file, user);
    });

    assertEquals("java.lang.RuntimeException: DB error", exception.getMessage());
    verify(jobService).updateJobStatus(100L, Status.FAILED);
  }
}