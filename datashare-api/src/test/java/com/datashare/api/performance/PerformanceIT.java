package com.datashare.api.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/** Integration Test Set for Performance */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PerformanceIT {

  @LocalServerPort private int port;

  /** Test that it is possible to handle 100 concurrent registrations */
  @Test
  @DisplayName("INTEG-REGISTER-001: Should handle 100 concurrent registrations")
  void testConcurrentRegistrations() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(100);
    CountDownLatch latch = new CountDownLatch(100);
    AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 0; i < 100; i++) {
      final int index = i;
      executor.submit(
          () -> {
            try {
              RestTemplate restTemplate = new RestTemplate();
              ResponseEntity<String> response =
                  restTemplate.postForEntity(
                      "http://localhost:" + port + "/auth/register",
                      Map.of("email", "user" + index + "@test.com", "password", "Pass123!"),
                      String.class);
              if (response.getStatusCode().is2xxSuccessful()) {
                successCount.incrementAndGet();
              }
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(successCount.get()).isGreaterThanOrEqualTo(95); // 95% de succès
  }
}
