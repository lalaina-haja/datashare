package com.datashare.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("unit")
class ContextLoadTest {

  @Test
  void contextLoads() {
    // If the context fails to start, this test fails
  }
}
