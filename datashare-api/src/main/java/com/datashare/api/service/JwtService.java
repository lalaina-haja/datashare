package com.datashare.api.service;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtEncoder jwtEncoder;
  private final long expirationSeconds;
  private final String issuer;

  public JwtService(
      JwtEncoder jwtEncoder,
      @Value("${jwt.expiration}") long expirationSeconds,
      @Value("${spring.application.name}") String issuer) {
    this.jwtEncoder = jwtEncoder;
    this.expirationSeconds = expirationSeconds;
    this.issuer = issuer;
  }

  public String generateToken(String userId) {

    Instant now = Instant.now();

    JwtClaimsSet claimsSet =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expirationSeconds))
            .subject(userId)
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }
}
