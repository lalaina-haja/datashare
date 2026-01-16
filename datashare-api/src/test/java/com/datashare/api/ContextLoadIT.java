package com.datashare.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("it")
public class ContextLoadIT {
  @Test
  void contextLoads() {
    // if Spring context fails, test fails
  }
}
