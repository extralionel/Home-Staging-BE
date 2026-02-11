package io.home.staging.model.response;

import io.home.staging.entity.PlanType;
import io.home.staging.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  // User information
  private Integer id;
  private String firstName;
  private String lastName;
  private String email;

  // Plan information
  private Integer creditsLeft;
  private Integer totalCredits;
  private PlanType planType;

  public UserResponse(User user) {
    this.id = user.getId();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.planType = user.getPlan().getType();
    this.creditsLeft = user.getCreditsLeft();
    this.totalCredits = user.getPlan().getCredits();
  }
}
