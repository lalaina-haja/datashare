package com.datashare.api.configuration;

import com.datashare.api.security.CsrfCookieFilter;
import com.datashare.api.security.CustomUserDetailService;
import com.datashare.api.security.JwtAuthenticationEntryPoint;
import com.datashare.api.security.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Spring Security configuration for the application.
 *
 * <p>Defines the security filter chain, authentication providers, and authorization rules.
 * Configures JWT-based OAuth2 resource server authentication, CSRF protection, and endpoint access
 * control. Authentication endpoints ({@code /auth/**}), actuator endpoints, and public files are
 * permit-all, while all other requests require JWT authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Autowired private final JwtAuthenticationFilter jwtAuthFilter;

  @Autowired private final CustomUserDetailService customUserDetailService;

  @Autowired private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Value("${web-url}")
  private String webUrl;

  /**
   * Provides an AuthenticationProvider that delegates to the application's {@link
   * CustomUserDetailService} and uses the configured password encoder.
   *
   * @return a configured {@link AuthenticationProvider} bean
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Exposes the framework-provided AuthenticationManager as a bean.
   *
   * <p>This allows the authentication manager to be injected elsewhere in the application, such as
   * in authentication endpoints or other security-related components.
   *
   * @param config the {@link AuthenticationConfiguration}
   * @return the application {@link AuthenticationManager}
   * @throws Exception if the authentication manager cannot be obtained
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configures the security filter chain for HTTP requests.
   *
   * <p>Permits public access to:
   *
   * <ul>
   *   <li>Authentication endpoints ({@code /auth/**})
   *   <li>Actuator endpoints ({@code /actuator/**})
   *   <li>Public files ({@code /files/public/**})
   * </ul>
   *
   * <p>All other requests require JWT authentication via OAuth2 resource server. CSRF protection is
   * disabled as JWT tokens are used instead.
   *
   * @param http the {@link HttpSecurity} object to configure
   * @return the configured {@link SecurityFilterChain}
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // Custom Handler for SPA compatible CSRF
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName("_csrf");
    http
        // ════════════════════════════════════════════════════
        // CSRF PROTECTION
        // ════════════════════════════════════════════════════
        .csrf(
            csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(requestHandler)
                    .ignoringRequestMatchers(
                        "/auth/login",
                        "/auth/register",
                        "/auth/logout",
                        "/actuator/**",
                        "/files/**",
                        "/auth/me"))
        // Add filter to send the CSRF token to frontend
        .addFilterAfter(new CsrfCookieFilter(), LogoutFilter.class)

        // ════════════════════════════════════════════════════
        // CORS - Centralised Configuration
        // ════════════════════════════════════════════════════
        .cors(
            cors ->
                cors.configurationSource(
                    request -> {
                      var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                      corsConfig.setAllowedOrigins(List.of(webUrl));
                      corsConfig.setAllowedMethods(
                          List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                      corsConfig.setAllowedHeaders(
                          List.of("*")); // cover all headers (Authorization, Content-Type, etc.)
                      corsConfig.setAllowCredentials(
                          true); // CRUCIAL for cookies (AUTH-TOKEN, XSRF-TOKEN)
                      corsConfig.setMaxAge(
                          3600L); // cache the responses preflight 1h → good for performance.
                      return corsConfig;
                    }))

        // ════════════════════════════════════════════════════
        // ENDPOINTS AUTORISATION
        // ════════════════════════════════════════════════════
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints
                    .requestMatchers(
                        "/auth/register",
                        "/auth/login",
                        "/auth/logout",
                        "/auth/me",
                        "/actuator/**",
                        "/files/download/**",
                        "/files/public/upload",
                        "/error")
                    .permitAll()
                    // Other paths need authentification
                    .anyRequest()
                    .authenticated())

        // ════════════════════════════════════════════════════
        // AUTHENTICATION ENTRY POINT
        // ════════════════════════════════════════════════════
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

        // ════════════════════════════════════════════════════
        // SESSION MANAGEMENT - Stateless
        // ════════════════════════════════════════════════════
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // ════════════════════════════════════════════════════
        // LOGOUT HANDLER
        // ════════════════════════════════════════════════════
        .logout(
            logout ->
                logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessHandler(
                        (request, response, authentication) -> {
                          ResponseCookie deleteAuth =
                              ResponseCookie.from("AUTH-TOKEN", "")
                                  .httpOnly(true)
                                  .secure(true)
                                  .sameSite("Strict")
                                  .path("/")
                                  .maxAge(0)
                                  .build();

                          ResponseCookie deleteXsrf =
                              ResponseCookie.from("XSRF-TOKEN", "")
                                  .httpOnly(false) // XSRF is not HttpOnly
                                  .secure(true)
                                  .sameSite("Strict")
                                  .path("/")
                                  .maxAge(0)
                                  .build();

                          response.addHeader("Set-Cookie", deleteAuth.toString());
                          response.addHeader("Set-Cookie", deleteXsrf.toString());
                          response.setStatus(200);
                        }))

        // ════════════════════════════════════════════════════
        // JWT FILTERS
        // ════════════════════════════════════════════════════
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Password encoder bean using BCrypt hashing algorithm.
   *
   * @return a {@link PasswordEncoder} instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
