package io.home.staging.validator;

import io.home.staging.entity.User;
import io.home.staging.model.request.PromptRequest;

public interface CreditsValidator {
  boolean validate(User user, PromptRequest request);
}
