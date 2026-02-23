package io.home.staging.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import io.home.staging.entity.Plan;
import io.home.staging.entity.PlanType;
import io.home.staging.entity.User;
import io.home.staging.repository.PlanRepository;
import io.home.staging.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StripeService {

  private static final Logger log = LoggerFactory.getLogger(StripeService.class);

  @Value("${stripe.webhook.secret}")
  private String endpointSecret;

  @Value("${stripe.return.url}")
  private String returnUrl;

  private final UserRepository userRepository;
  private final PlanRepository planRepository;

  public StripeService(UserRepository userRepository, PlanRepository planRepository) {
    this.userRepository = userRepository;
    this.planRepository = planRepository;
  }

  public String createCheckoutSession(User user, PlanType planType) throws StripeException {
    // Find the requested plan in DB to ensure it exists
    Plan plan = planRepository.findByType(planType)
        .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planType));

    // Let's establish some basic pricing logic for the plans
    // In a real app, you would use Stripe Price IDs
    long amount = 0;
    switch (planType) {
      case PRO:
        amount = 1000; // $10.00
        break;
      case ENTERPRISE:
        amount = 5000; // $50.00
        break;
      default:
        throw new IllegalArgumentException("Cannot create checkout for FREE plan");
    }

    SessionCreateParams params = SessionCreateParams.builder()
        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
        .setReturnUrl(returnUrl)
        .setClientReferenceId(user.getId().toString())
        .putMetadata("planType", planType.name())
        .setMode(Mode.PAYMENT) // Or SUBSCRIPTION
        .addLineItem(
            SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(amount)
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Home Staging " + planType.name() + " Plan")
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();



    try {
      Session session = Session.create(params);
      return session.getClientSecret();
    } catch (Exception e) {
      log.warn("Exception message: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Transactional
  public void processWebhook(String payload, String sigHeader) {
    Event event;

    try {
      event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
    } catch (Exception e) {
      log.error("Webhook error while validating signature.", e);
      throw new IllegalArgumentException("Webhook error: " + e.getMessage());
    }

    switch (event.getType()) {
      case "checkout.session.completed":
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session != null) {
          handleSessionCompleted(session);
        }
        break;
      // Handle other events as needed
      default:
        log.info("Unhandled event type: " + event.getType());
    }
  }

  private void handleSessionCompleted(Session session) {
    String userIdStr = session.getClientReferenceId();
    if (userIdStr == null) {
      log.warn("No client_reference_id found in Session");
      return;
    }

    String planTypeStr = session.getMetadata().get("planType");
    if (planTypeStr == null) {
      log.warn("No planType found in Session metadata");
      return;
    }

    PlanType planTypeStrEnum;
    try {
      planTypeStrEnum = PlanType.valueOf(planTypeStr);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid planType in metadata: " + planTypeStr);
      return;
    }

    Integer userId = Integer.parseInt(userIdStr);
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      log.warn("User not found: " + userId);
      return;
    }

    Optional<Plan> planOpt = planRepository.findByType(planTypeStrEnum);
    if (planOpt.isEmpty()) {
      log.warn("Plan not found: " + planTypeStrEnum);
      return;
    }

    User user = userOpt.get();
    Plan newPlan = planOpt.get();

    user.setPlan(newPlan);
    // Add credits or reset credits based on the new plan
    user.setCreditsLeft(user.getCreditsLeft() + newPlan.getCredits());
    // Might reset generation limit or update it
    // We'll set the generationsLeft to the new plan's limit, or add to it. Let's reset for now to daily limit.
    user.setGenerationsLeft(newPlan.getDailyGenerationLimit());

    userRepository.save(user);

    log.info("Successfully updated user {} to plan {}", user.getId(), newPlan.getType());
  }
}
