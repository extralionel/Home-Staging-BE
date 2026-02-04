package io.home.staging.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {
  private String roomType;
  private int numberImages;
  private String additionalDetails;
  private String style;
  private String imageId;
}
