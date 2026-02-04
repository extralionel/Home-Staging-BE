package io.home.staging.service;

import io.home.staging.entity.Role;
import io.home.staging.model.request.LoginRequest;
import io.home.staging.model.request.RegisterRequest;
import io.home.staging.model.response.AuthenticationResponse;
import io.home.staging.entity.User;
import io.home.staging.repository.TokenRepository;
import io.home.staging.entity.Token;
import io.home.staging.entity.TokenType;
import io.home.staging.repository.UserRepository;
import io.home.staging.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {
    User user = User.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .build();

    user = userRepository.save(user);
    String jwtToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
        .refreshToken(refreshToken)
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .build();
  }

  public AuthenticationResponse authenticate(LoginRequest request) {
    Authentication authenticate = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
            request.getPassword()));

    User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
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
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
            .setAudience(Collections.singletonList(googleClientId))
            .build();

        GoogleIdToken idToken = verifier.verify(credential);
        if (idToken != null) {
          GoogleIdToken.Payload payload = idToken.getPayload();
          return processGoogleUser(
              payload.getEmail(),
              (String) payload.get("given_name"),
              (String) payload.get("family_name"));
        }
      }

      // If not a JWT or verification failed, try as Access Token
      return authenticateWithAccessToken(credential);

    } catch (Exception e) {
      throw new RuntimeException("Failed to verify Google Token", e);
    }
  }

  private AuthenticationResponse authenticateWithAccessToken(String accessToken) throws IOException {
    NetHttpTransport transport = new NetHttpTransport();
    com.google.api.client.http.HttpRequestFactory requestFactory = transport.createRequestFactory();
    com.google.api.client.http.GenericUrl url = new com.google.api.client.http.GenericUrl(
        "https://www.googleapis.com/oauth2/v3/userinfo");
    url.put("access_token", accessToken);

    com.google.api.client.http.HttpRequest request = requestFactory.buildGetRequest(url);
    com.google.api.client.http.HttpResponse response = request.execute();

    if (response.isSuccessStatusCode()) {
      java.util.Map<String, Object> payload = new com.google.api.client.json.jackson2.JacksonFactory()
          .createJsonParser(response.getContent())
          .parse(java.util.Map.class);

      return processGoogleUser(
          (String) payload.get("email"),
          (String) payload.get("given_name"),
          (String) payload.get("family_name"));
    } else {
      throw new RuntimeException("Failed to fetch user info from Google with access token");
    }
  }

  private AuthenticationResponse processGoogleUser(String email, String firstName, String lastName) {
    User user = userRepository.findByEmail(email).orElseGet(() -> {
      User newUser = User.builder()
          .firstName(firstName)
          .lastName(lastName)
          .email(email)
          .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
          .role(Role.USER)
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
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.userRepository.findByEmail(userEmail)
          .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
