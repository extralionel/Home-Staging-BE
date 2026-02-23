package io.home.staging.controller;

import com.stripe.exception.StripeException;
import io.home.staging.entity.User;
import io.home.staging.model.request.CreateCheckoutSessionRequest;
import io.home.staging.model.response.CheckoutSessionResponse;
import io.home.staging.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final StripeService stripeService;

  public PaymentController(StripeService stripeService) {
    this.stripeService = stripeService;
  }

  @PostMapping("/create-checkout-session")
  public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
      @AuthenticationPrincipal User user,
      @RequestBody CreateCheckoutSessionRequest request) {
    try {
      String clientSecret = stripeService.createCheckoutSession(user, request.getPlanType());
      return ResponseEntity.ok(CheckoutSessionResponse.builder().clientSecret(clientSecret).build());
    } catch (StripeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/webhook")
  public ResponseEntity<Void> processWebhook(
      @RequestBody String payload,
      @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
      stripeService.processWebhook(payload, sigHeader);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
