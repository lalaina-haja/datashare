package com.datashare.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;

/**
 * Unit tests for JWT encoding functionality.
 *
 * <p>This test class validates that the JWT encoder correctly signs and encodes JWT tokens with the
 * expected claims and headers.
 */
@SpringBootTest
@ActiveProfiles("unit")
public class JwtEncoderTest {
  /** The JWT encoder instance to be tested. */
  @Autowired JwtEncoder jwtEncoder;

  /**
   * Tests that the JWT encoder successfully signs a token with the expected claims.
   *
   * <p>This test creates a JWT with a subject, issued-at timestamp, and expiration time, then
   * verifies that the encoder produces a non-null token value.
   */
  @Test
  void jwtEncoderShouldSignToken() {

    // GIVEN the claim set
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject("test")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

    // WHEN the encode encode the claim set
    Jwt jwt =
        jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(() -> "HS256").build(), claims));

    // THEN a jwt is correctly encoded
    assertNotNull(jwt.getTokenValue());
    assertThat(jwt.getSubject()).isEqualTo("test");
  }
}
