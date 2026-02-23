package io.home.staging.config;

import io.home.staging.entity.ImageQuality;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.pricing")
public class PricingConfig {

  private Map<ImageQuality, Integer> imageQuality = new HashMap<>();

  public Map<ImageQuality, Integer> getImageQuality() {
    return imageQuality;
  }

  public void setImageQuality(Map<ImageQuality, Integer> map) {
    this.imageQuality = map;
  }

  public int getCreditCostByQuality(ImageQuality type) {
    return imageQuality.get(type);
  }
}
