package com.datashare.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("unit")
public class JwtServiceTest {

  private static final String USER_ID = "test-subject";

  @Mock private JwtEncoder jwtEncoder;

  private JwtService jwtService;

  @BeforeEach
  void setup() {
    jwtService = new JwtService(jwtEncoder, 3600L, "datashare-api");
  }

  @Test
  public void get_token_successful() {

    // GIVEN the user ID and the token
    String expectedToken = "token";
    Jwt jwt =
        new Jwt(
            expectedToken,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of("sub", USER_ID));
    when(jwtEncoder.encode(any())).thenReturn(jwt);

    // WHEN generating a token
    String returnedToken = jwtService.generateToken(USER_ID);

    // THEN the token is returned successfully
    assertThat(returnedToken).isEqualTo(expectedToken);
  }
}
