package io.home.staging.validator;

import io.home.staging.config.PricingConfig;
import io.home.staging.entity.ImageQuality;
import io.home.staging.entity.User;
import io.home.staging.exception.ErrorCode;
import io.home.staging.exception.InsufficientCreditsException;
import io.home.staging.model.request.PromptRequest;
import io.home.staging.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserCreditsValidator implements CreditsValidator {

  private final PricingConfig pricingConfig;
  private final UserRepository userRepository;

  public UserCreditsValidator(PricingConfig pricingConfig, UserRepository userRepository) {
    this.pricingConfig = pricingConfig;
    this.userRepository = userRepository;
  }

  @Override
  public boolean validate(User user, PromptRequest request) {
    ImageQuality quality = request.getQuality();
    quality = ImageQuality.STANDARD;

    int quantity = request.getNumberImages();
    int credits = pricingConfig.getCreditCostByQuality(quality);
    int totalCredits = quantity * credits;

    Integer creditsLeft = user.getCreditsLeft();
    if (totalCredits > creditsLeft) {
      throw new InsufficientCreditsException("User has not enough credits.", ErrorCode.INSUFFICIENT_CREDITS);
    }

    Integer generationsLeft = user.getGenerationsLeft();
    if (generationsLeft < 1) {
      throw new InsufficientCreditsException("User has reached the daily generation limit.", ErrorCode.INSUFFICIENT_CREDITS);
    }

    userRepository.decrementDailyGenerations(user.getId());

    return true;
  }
}
