package io.home.staging.controller;

import io.home.staging.model.request.UserProfileRequest;
import io.home.staging.model.response.UserResponse;
import io.home.staging.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) {
    return ResponseEntity.ok(userService.getUserProfile(authentication));
  }
}
