package com.datashare.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  private static final String COOKIE_NAME = "AUTH-TOKEN";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // ════════════════════════════════════════════════════
    // Extraction du JWT depuis le cookie HttpOnly
    // ════════════════════════════════════════════════════
    String jwt = extractJwtFromCookie(request);

    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // ════════════════════════════════════════════════════
      // Validation et extraction du username
      // ════════════════════════════════════════════════════
      String userEmail = jwtService.extractUsername(jwt);

      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // ════════════════════════════════════════════════════
        // Validation du token
        // ════════════════════════════════════════════════════
        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authToken);

          log.debug("User {} authenticated successfully", userEmail);
        } else {
          log.warn("Invalid JWT token for user {}", userEmail);
        }
      }
    } catch (IllegalArgumentException e) {
      // Token vide ou null
      log.error("JWT token is empty: {}", e.getMessage());
    } catch (Exception e) {
      // Autres erreurs
      log.error("Cannot set user authentication: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  /** Extrait le JWT depuis le cookie AUTH-TOKEN */
  private String extractJwtFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
