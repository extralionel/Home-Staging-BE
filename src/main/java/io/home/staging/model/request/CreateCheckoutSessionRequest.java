package io.home.staging.model.request;

import io.home.staging.entity.PlanType;
import lombok.Data;

@Data
public class CreateCheckoutSessionRequest {
    private PlanType planType;
}
