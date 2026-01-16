package com.datashare.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Unit test for Spring Boot application context loading.
 *
 * <p>This test verifies that the Spring application context can be successfully loaded with the
 * "unit" profile active. It serves as a smoke test to ensure all beans are properly configured and
 * can be instantiated without errors.
 */
@SpringBootTest
@ActiveProfiles("unit")
class ContextLoadTest {

  /**
   * Tests that the Spring application context loads successfully.
   *
   * <p>This is a smoke test that verifies the application context can start without errors. If the
   * context fails to start, this test fails immediately.
   */
  @Test
  void contextLoads() {
    // If the context fails to start, this test fails
  }
}
