package com.datashare.api.service;

import com.datashare.api.entities.File;
import com.datashare.api.entities.Token;
import com.datashare.api.handler.InvalidTokenException;
import com.datashare.api.repository.FileRepository;
import com.datashare.api.repository.TokenRepository;
import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  @Autowired private TokenRepository tokenRepository;

  @Autowired private FileRepository fileRepository;

  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int TOKEN_LENGTH = 6;

  public Token generateToken(File file, Instant expiresAt) {

    Token token = new Token();
    token.setTokenString(randomToken());
    token.setExpiresAt(expiresAt);
    token.setFile(file);
    file.setToken(token);

    this.fileRepository.save(file);

    return token;
  }

  public File validateToken(String tokenString) throws Exception {
    File file = tokenRepository.findByTokenString(tokenString).map(Token::getFile).orElse(null);

    if (file == null) {
      throw new InvalidTokenException("Unknown token");
    }

    if (file.getToken().getExpiresAt().isBefore(Instant.now())) {
      throw new InvalidTokenException("Expired token");
    }

    return file;
  }

  private String randomToken() {
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
    for (int i = 0; i < TOKEN_LENGTH; i++) {
      sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return sb.toString();
  }
}
