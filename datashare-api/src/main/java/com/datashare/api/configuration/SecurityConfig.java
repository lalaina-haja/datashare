package com.datashare.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration for Spring Security.
 *
 * <p>This class defines the security filter chain for the application, configuring CSRF protection,
 * authorization rules, and OAuth2 JWT resource server.
 */
@Configuration
public class SecurityConfig {

  /**
   * Configures the security filter chain for HTTP requests.
   *
   * <p>Permits public access to authentication endpoints, health/info actuators, and public files.
   * All other requests require authentication via JWT.
   *
   * @param http the HttpSecurity object to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/auth/**", "/actuator/health", "/actuator/info", "/files/public/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }
}
