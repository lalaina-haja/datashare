package com.datashare.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datashare.api.entities.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

/** Unit Test Set for JWT Authentication Filter */
@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;

  @Mock private UserDetailsService userDetailsService;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private User testUser;
  private String validToken;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();

    // Clear SecurityContext before each test
    SecurityContextHolder.clearContext();

    // Setup test user
    testUser = new User(1L, "test@example.com", "hashedPassword", null);

    validToken = "valid.jwt.token";
  }

  /** Test that with a valid token, the context is authenticated */
  @Test
  @DisplayName("UNIT-JWT-FILTER-001: Should authenticate valid JWT token")
  void testValidJwtAuthentication() throws ServletException, IOException {

    // GIVEN cookie with valid token
    Cookie authCookie = new Cookie("AUTH-TOKEN", validToken);
    request.setCookies(authCookie);

    when(jwtService.extractUsername(validToken)).thenReturn(testUser.getEmail());
    when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
    when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

    // WHEN filter
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // THEN authenticated
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .isEqualTo(testUser);
    assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtService, times(1)).extractUsername(validToken);
    verify(userDetailsService, times(1)).loadUserByUsername(testUser.getEmail());
    verify(jwtService, times(1)).isTokenValid(validToken, testUser);
  }

  /** Test that without AUTH token the context is ignored */
  @Test
  @DisplayName("UNIT-JWT-FILTER-002: Should ignore if no cookie AUTH-TOKEN")
  void testNoCookiePresent() throws ServletException, IOException {

    // GIVEN no cookies

    // WHEN filter
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // THEN not authenticated
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtService, never()).extractUsername(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  /** Test that the context is rejected if isTokenValid returns false */
  @Test
  @DisplayName(
      "UNIT-JWT-FILTER-003: Devrait rejeter si token valide mais isTokenValid retourne false")
  void testTokenNotValidForUser() throws ServletException, IOException {

    // GIVEN an AUTH-TOKEN cookie
    Cookie authCookie = new Cookie("AUTH-TOKEN", validToken);
    request.setCookies(authCookie);

    // AND the isTokenValid return false
    when(jwtService.extractUsername(validToken)).thenReturn(testUser.getEmail());
    when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
    when(jwtService.isTokenValid(validToken, testUser)).thenReturn(false);

    // WHEN filter
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // THEN the context is not authenticated
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, times(1)).doFilter(request, response);
    verify(jwtService, times(1)).isTokenValid(validToken, testUser);
  }

  /** Test that the filter can handle inexisting user */
  @Test
  @DisplayName("UNIT-JWT-FILTER-004: Should handle inexisting user")
  void testUserNotFound() throws ServletException, IOException {

    // GIVEN the valid AUTH token cookie
    Cookie authCookie = new Cookie("AUTH-TOKEN", validToken);
    request.setCookies(authCookie);

    // AND an inexisting user
    when(jwtService.extractUsername(validToken)).thenReturn("nonexistent@example.com");
    when(userDetailsService.loadUserByUsername("nonexistent@example.com"))
        .thenThrow(
            new org.springframework.security.core.userdetails.UsernameNotFoundException(
                "User not found"));

    // WHEN filter
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // THEN the context in not authenticated
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, times(1)).doFilter(request, response);
  }

  /** Test that the filter can handle multiple cookies */
  @Test
  @DisplayName("UNIT-JWT-FILTER-005: Should handle multiple cookies including AUTH-TOKEN")
  void testMultipleCookies() throws ServletException, IOException {

    // GIVEN the cookies
    Cookie sessionCookie = new Cookie("SESSION", "session-value");
    Cookie authCookie = new Cookie("AUTH-TOKEN", validToken);
    Cookie csrfCookie = new Cookie("XSRF-TOKEN", "csrf-value");
    request.setCookies(sessionCookie, authCookie, csrfCookie);

    when(jwtService.extractUsername(validToken)).thenReturn(testUser.getEmail());
    when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
    when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

    // WHEN filter
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // THEN the context is authenticated
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    verify(filterChain, times(1)).doFilter(request, response);
  }
}
