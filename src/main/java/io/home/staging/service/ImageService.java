package io.home.staging.service;

import com.cloudinary.Cloudinary;
import io.home.staging.entity.Image;
import io.home.staging.entity.Project;
import io.home.staging.entity.User;
import io.home.staging.model.response.ImageResponse;
import io.home.staging.repository.ImageRepository;
import io.home.staging.repository.ProjectRepository;
import io.home.staging.repository.UserRepository;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

  /**
   * PAYLOAD UPLOAD IMAGE RESPONSE:
   * {
   * "asset_id": "86ca8ba13b17e21d23534b7e842b8847",
   * "public_id": "do8wnccnlzrfvwv1mqkq",
   * "version": 1719309138,
   * "version_id": "1a2b0a8ef0bf8e9f20a922f38704eda6",
   * "signature": "afb6a3374ba12e4e0307e23e625d939b242ddb5c",
   * "width": 1920,
   * "height": 1281,
   * "format": "jpg",
   * "resource_type": "image",
   * "created_at": "2024-06-25T09:52:18Z",
   * "tags": [],
   * "bytes": 310479,
   * "type": "upload",
   * "etag": "a8f8236455d352b8cee6aba0e3fbc87e",
   * "placeholder": false,
   * "url":
   * "http://res.cloudinary.com/cld-docs/image/upload/v1719309138/do8wnccnlzrfvwv1mqkq.jpg",
   * "secure_url":
   * "https://res.cloudinary.com/cld-docs/image/upload/v1719309138/do8wnccnlzrfvwv1mqkq.jpg",
   * "asset_folder": "",
   * "display_name": "do8wnccnlzrfvwv1mqkq",
   * "original_filename": "f5lq8lfq8pfj0xmd9dak",
   * "api_key": "614335564976464"
   * }
   */
  private final Cloudinary cloudinary;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final ImageRepository imageRepository;

  public ImageService(
      Cloudinary cloudinary,
      UserRepository userRepository,
      ProjectRepository projectRepository,
      ImageRepository imageRepository) {
    this.cloudinary = cloudinary;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.imageRepository = imageRepository;
  }

  public Image uploadImage(Authentication authentication, MultipartFile file) {
    try {
      return uploadImage(authentication, null, file.getBytes());
    } catch (IOException e) {
      throw new RuntimeException("Error while deserializing image", e);
    }
  }

  /**
   * Upload image to Cloudinary
   */
  public Image uploadImage(Authentication authentication, Project project, byte[] file) {
    String email = authentication.getName();
    User user = userRepository.findByEmailOrThrow(email);

    try {
      Map result = cloudinary.uploader().upload(file, Map.of());
      String publicId = result.get("public_id").toString();
      String url = result.get("url").toString();

      Image image = new Image(url, publicId, project);
      project.addImage(image);
      projectRepository.save(project);
      return image;
    } catch (Exception e) {
      throw new RuntimeException("Error while trying to upload image to Cloudinary.", e);
    }
  }

  public void deleteFile(String publicId) {
    Image image = imageRepository.findByPublicIdOrThrow(publicId);
    try {
      imageRepository.delete(image);
      cloudinary.uploader().destroy(publicId, Map.of());
    } catch (IOException e) {
      throw new RuntimeException("Error al eliminar la imagen", e);
    }
  }
}
