package com.datashare.api.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Component that logs active Spring profiles on application startup.
 *
 * <p>Automatically logs which profiles are active when the application context is initialized,
 * useful for debugging environment-specific configurations.
 */
@Slf4j
@Component
public class ActiveProfileLogger {
  /**
   * Constructs the ActiveProfileLogger and logs active profiles.
   *
   * @param environment the Spring environment providing access to active profiles
   */
  public ActiveProfileLogger(Environment environment) {
    log.info("Active Spring profiles: {}", String.join(", ", environment.getActiveProfiles()));
  }
}
