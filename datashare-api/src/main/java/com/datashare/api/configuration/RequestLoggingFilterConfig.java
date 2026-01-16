package com.datashare.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuration for request logging.
 *
 * <p>Sets up a CommonsRequestLoggingFilter to log HTTP request details including query strings and
 * payloads for debugging and monitoring purposes.
 */
@Configuration
public class RequestLoggingFilterConfig {
  /**
   * Creates and configures a CommonsRequestLoggingFilter bean.
   *
   * <p>Enables logging of query strings, request payloads (up to 10000 characters), and excludes
   * header logging for privacy.
   *
   * @return the configured CommonsRequestLoggingFilter
   */
  @Bean
  public CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setMaxPayloadLength(10000);
    filter.setIncludeHeaders(false);
    filter.setAfterMessagePrefix("REQUEST DATA: ");
    return filter;
  }
}
