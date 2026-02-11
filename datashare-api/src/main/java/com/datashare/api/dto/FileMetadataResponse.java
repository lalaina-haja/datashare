package com.datashare.api.dto;

import com.datashare.api.entities.File;
import com.datashare.api.entities.Token;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataResponse {

  String filename;
  String contentType;
  long size;
  String downloadToken;
  Instant createdAt;
  Instant expiresAt;

  public static FileMetadataResponse fromEntity(File f) {

    Token token = f.getToken();

    return new FileMetadataResponse(
        f.getFilename(),
        f.getContentType(),
        f.getSize(),
        token != null ? f.getToken().getTokenString() : null,
        f.getCreatedAt(),
        token != null ? f.getToken().getExpiresAt() : null);
  }
}
