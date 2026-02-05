package com.datashare.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {
  @Override
  public void logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    log.info("Logging out user: {}", authentication != null ? authentication.getName() : "unknown");

    // ════════════════════════════════════════════════════
    // Suppression du cookie AUTH-TOKEN
    // ════════════════════════════════════════════════════
    ResponseCookie deleteCookie =
        ResponseCookie.from("AUTH-TOKEN", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(Duration.ZERO) // Expire immédiatement
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
  }
}
