package io.home.staging.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.home.staging.config.PricingConfig;
import io.home.staging.entity.ImageQuality;
import io.home.staging.entity.User;
import io.home.staging.exception.ErrorCode;
import io.home.staging.exception.InsufficientCreditsException;
import io.home.staging.model.request.PromptRequest;
import io.home.staging.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserCreditsValidatorTest {

  @Mock
  private PricingConfig pricingConfig;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserCreditsValidator userCreditsValidator;

  @Test
  void testValidate_Success() {
    User user = new User();
    user.setId(1);
    user.setCreditsLeft(10);
    user.setGenerationsLeft(5);

    PromptRequest request = new PromptRequest();
    request.setQuality(ImageQuality.STANDARD);
    request.setNumberImages(2);

    when(pricingConfig.getCreditCostByQuality(ImageQuality.STANDARD)).thenReturn(2);

    boolean result = userCreditsValidator.validate(user, request);

    assertTrue(result);
    // Verifying that after validation the daily generation count is updated
    verify(userRepository).decrementDailyGenerations(1);
    // save
  }

  @Test
  void testValidate_InsufficientCredits() {
    User user = new User();
    user.setId(1);
    user.setCreditsLeft(3); // Less than totalCredits (which is 2 * 2 = 4)

    PromptRequest request = new PromptRequest();
    request.setQuality(ImageQuality.STANDARD);
    request.setNumberImages(2);

    when(pricingConfig.getCreditCostByQuality(ImageQuality.STANDARD)).thenReturn(2);

    InsufficientCreditsException exception = assertThrows(
        InsufficientCreditsException.class,
        () -> userCreditsValidator.validate(user, request)
    );

    assertTrue(exception.getMessage().contains("User has not enough credits."));
    assertEquals(ErrorCode.INSUFFICIENT_CREDITS, exception.getErrorCode());
  }

  @Test
  void testValidate_DailyGenerationLimitReached() {
    User user = new User();
    user.setId(1);
    user.setCreditsLeft(10);
    user.setGenerationsLeft(0); // Limit reached

    PromptRequest request = new PromptRequest();
    request.setQuality(ImageQuality.STANDARD);
    request.setNumberImages(2);

    when(pricingConfig.getCreditCostByQuality(ImageQuality.STANDARD)).thenReturn(2);

    InsufficientCreditsException exception = assertThrows(
        InsufficientCreditsException.class,
        () -> userCreditsValidator.validate(user, request)
    );

    assertTrue(exception.getMessage().contains("User has reached the daily generation limit."));
    assertEquals(ErrorCode.INSUFFICIENT_CREDITS, exception.getErrorCode());
  }
}
