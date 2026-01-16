package com.datashare.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for Spring Boot application context loading.
 *
 * <p>This test verifies that the Spring application context can be successfully loaded with the
 * "it" (integration test) profile active. It ensures that all beans and external service
 * integrations are properly configured for integration testing.
 */
@SpringBootTest
@ActiveProfiles("it")
public class ContextLoadIT {
  /**
   * Tests that the Spring application context loads successfully with integration test profile.
   *
   * <p>This integration test verifies that the application context can start with the "it" profile
   * and all necessary integrations are available. If the context fails to start, this test fails.
   */
  @Test
  void contextLoads() {
    // if Spring context fails, test fails
  }
}
