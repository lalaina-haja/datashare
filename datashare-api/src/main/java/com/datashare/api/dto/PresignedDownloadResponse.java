package com.datashare.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedDownloadResponse {

  String filename;

  String contentType;

  long size;

  String downloadUrl;

  Instant createdAt;

  Instant expiresAt;
}
