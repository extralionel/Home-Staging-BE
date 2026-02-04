package io.home.staging.model.response;

import io.home.staging.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

  private String imageId;
  private String url;

  public ImageResponse(Image image) {
    this.imageId = image.getPublicId();
    this.url = image.getUrl();
  }
}
