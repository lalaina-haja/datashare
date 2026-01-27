package com.datashare.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filtre qui force la génération du cookie CSRF pour le frontend */
public class CsrfCookieFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

    // Force la génération du cookie en appelant getToken()
    if (csrfToken != null) {
      csrfToken.getToken();
    }

    filterChain.doFilter(request, response);
  }
}
