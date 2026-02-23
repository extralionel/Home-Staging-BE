package io.home.staging.service;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import io.home.staging.entity.Plan;
import io.home.staging.entity.PlanType;
import io.home.staging.entity.Role;
import io.home.staging.entity.VerificationToken;
import io.home.staging.model.request.LoginRequest;
import io.home.staging.model.request.RegisterRequest;
import io.home.staging.model.response.AuthenticationResponse;
import io.home.staging.entity.User;
import io.home.staging.repository.PlanRepository;
import io.home.staging.repository.TokenRepository;
import io.home.staging.entity.Token;
import io.home.staging.entity.TokenType;
import io.home.staging.repository.UserRepository;
import io.home.staging.security.JwtService;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import java.io.IOException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.home.staging.model.request.GoogleLoginRequest;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  @Value("${spring.application.security.google.client-id}")
  private String googleClientId;

  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final PlanRepository planRepository;

  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final io.home.staging.repository.VerificationTokenRepository verificationTokenRepository;
  private final EmailService emailService;

  public AuthenticationResponse register(RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new EntityExistsException("User with email " + request.getEmail() + " already exists");
    }

    Plan plan = planRepository.findByPlanTypeOrThrow(PlanType.FREE);
    User user = User.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .plan(plan)
        .creditsLeft(plan.getCredits())
        .generationsLeft(plan.getDailyGenerationLimit())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .enabled(true) // TODO: Change this to use account verification
        .build();
    user = userRepository.save(user);

    String token = UUID.randomUUID().toString();
    VerificationToken verificationToken = VerificationToken.builder()
        .token(token)
        .user(user)
        .expiryDate(LocalDateTime.now().plusHours(24))
        .build();
    verificationTokenRepository.save(verificationToken);

    String verificationUrl = "http://localhost:8080/api/v1/auth/verify?token=" + token;
    //emailService.sendVerificationEmail(user.getEmail(), verificationUrl);

    return AuthenticationResponse.builder()
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .message("User registered successfully. Please check your email to verify your account.")
        .build();
  }

  public AuthenticationResponse authenticate(LoginRequest request) {
    Authentication authenticate = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
    if (!user.isEnabled()) {
      throw new IllegalStateException("User not verified");
    }

    String token = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    revokeAllUserTokens(user);
    saveUserToken(user, token);

    return AuthenticationResponse.builder()
        .accessToken(token)
        .refreshToken(refreshToken)
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .build();
  }

  public AuthenticationResponse authenticate(GoogleLoginRequest request) {
    try {
      String credential = request.getCredential();

      // Try to verify as ID Token first (JWT)
      if (credential != null && credential.split("\\.").length == 3) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
            new JacksonFactory()).setAudience(Collections.singletonList(googleClientId)).build();

        GoogleIdToken idToken = verifier.verify(credential);
        if (idToken != null) {
          GoogleIdToken.Payload payload = idToken.getPayload();
          return processGoogleUser(payload.getEmail(), (String) payload.get("given_name"),
              (String) payload.get("family_name"));
        }
      }

      // If not a JWT or verification failed, try as Access Token
      return authenticateWithAccessToken(credential);

    } catch (Exception e) {
      throw new RuntimeException("Failed to verify Google Token", e);
    }
  }

  private AuthenticationResponse authenticateWithAccessToken(String accessToken)
      throws IOException {
    NetHttpTransport transport = new NetHttpTransport();
    HttpRequestFactory requestFactory = transport.createRequestFactory();
    GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
    url.put("access_token", accessToken);

    HttpRequest request = requestFactory.buildGetRequest(url);
    HttpResponse response = request.execute();

    if (response.isSuccessStatusCode()) {
      Map<String, Object> payload = new JacksonFactory().createJsonParser(
          response.getContent()).parse(Map.class);

      return processGoogleUser((String) payload.get("email"), (String) payload.get("given_name"),
          (String) payload.get("family_name"));
    } else {
      throw new RuntimeException("Failed to fetch user info from Google with access token");
    }
  }

  private AuthenticationResponse processGoogleUser(String email, String firstName,
      String lastName) {
    User user = userRepository.findByEmail(email).orElseGet(() -> {
      Plan plan = planRepository.findByPlanTypeOrThrow(PlanType.FREE);
      User newUser = User.builder()
          .firstName(firstName)
          .lastName(lastName)
          .email(email)
          .plan(plan)
          .creditsLeft(plan.getCredits())
          .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
          .role(Role.USER)
          .enabled(true)
          .build();
      return userRepository.save(newUser);
    });

    String jwtToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);

    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .build();
  }

  public AuthenticationResponse verifyEmail(String token) {
    io.home.staging.entity.VerificationToken verificationToken = verificationTokenRepository.findByToken(
            token)
        .orElseThrow(() -> new RuntimeException("Invalid verification token"));

    if (verificationToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
      throw new RuntimeException("Verification token has expired");
    }

    User user = verificationToken.getUser();
    user.setEnabled(true);
    userRepository.save(user);

    verificationTokenRepository.delete(verificationToken);

    return AuthenticationResponse.builder()
        .message("Email verified successfully. You can now log in.")
        .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    Token token = Token.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty()) {
      return;
    }
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });

    tokenRepository.saveAllAndFlush(validUserTokens);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }

    String refreshToken = authHeader.substring(7);
    String email = jwtService.extractUsername(refreshToken);
    if (email != null) {
      User user = userRepository.findByEmailOrThrow(email);
      if (jwtService.isTokenValid(refreshToken, user)) {
        String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
