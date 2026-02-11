package com.datashare.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadResponse {

  /** Presigned upload URL */
  String uploadUrl;

  /** Download token string */
  String tokenString;

  /** Token expiration */
  Instant expiresAt;
}
