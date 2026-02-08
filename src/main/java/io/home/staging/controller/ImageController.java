package io.home.staging.controller;

import io.home.staging.model.response.ImageResponse;
import io.home.staging.service.ImageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/image")
public class ImageController {

  private final ImageService imageService;

  @Autowired
  public ImageController(ImageService imageService) {
    this.imageService = imageService;
  }

  @PostMapping
  public ResponseEntity<ImageResponse> uploadImage(
      Authentication authentication,
      @RequestBody MultipartFile file
  ) {
    return ResponseEntity.ok(new ImageResponse(imageService.uploadImage(authentication, file)));
  }

  @GetMapping("/{imageId}")
  public ResponseEntity<String> getImage(@PathVariable String imageId) {
    return ResponseEntity.ok(imageId);
  }

}
