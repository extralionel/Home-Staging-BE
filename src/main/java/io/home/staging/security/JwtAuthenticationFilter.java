package io.home.staging.security;

import io.home.staging.entity.Token;
import io.home.staging.entity.User;
import io.home.staging.repository.TokenRepository;
import io.home.staging.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final UserRepository  userRepository;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
      if (request.getServletPath().contains("/auth")) {
        filterChain.doFilter(request, response);
        return;
      }

      final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      final String jwt = authHeader.substring(7);
      final String userEmail = jwtService.extractUsername(jwt);

      // 2. Validate token and set context
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        Token token = tokenRepository.findByToken(jwt).orElse(null);
        if (token == null || !token.isValid()) {
          filterChain.doFilter(request, response);
          return;
        }

        User user = userRepository.findByEmailOrThrow(userEmail);
        if (jwtService.isTokenValid(jwt, user)) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          // 3. Update SecurityContextHolder
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

      filterChain.doFilter(request, response);
    }
}