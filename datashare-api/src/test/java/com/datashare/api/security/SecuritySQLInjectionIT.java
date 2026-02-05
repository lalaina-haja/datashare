package com.datashare.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Integration Test Set for SQL Injection */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecuritySQLInjectionIT {

  @Autowired private MockMvc mockMvc;

  /** Test that SQL injection in the emais is rejected */
  @Test
  @DisplayName("INTEG-XSS-001: Should reject SQL injection tentatives in email")
  void testSQLInjectionInEmail() throws Exception {
    String maliciousEmail = "admin'--";

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{
    "email": "%s",
    "password": "ValidPass123!"
}
"""
                        .formatted(maliciousEmail)))
        .andExpect(status().isBadRequest());
  }
}
