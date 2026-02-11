package com.datashare.api.controller;

import com.datashare.api.dto.PresignedDownloadResponse;
import com.datashare.api.dto.PresignedUploadRequest;
import com.datashare.api.dto.PresignedUploadResponse;
import com.datashare.api.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoints for anonymous file operations (no authentication) */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicFileController {

  private final FileService fileService;

  @PostMapping("/public/upload")
  public ResponseEntity<PresignedUploadResponse> presignedUploadAnonymous(
      @RequestBody PresignedUploadRequest body) {
    // Create upload metadata without user association (userId = null)
    PresignedUploadResponse resp =
        fileService.createUploadUrl(
            body.getFilename(), body.getContentType(), body.getSize(), body.getExpirationDays(), null);

    return ResponseEntity.ok(resp);
  }

  @GetMapping("/public/download/{tokenString}")
  public ResponseEntity<PresignedDownloadResponse> presignedDownloadPublic(
      @PathVariable String tokenString) throws Exception {
    return ResponseEntity.ok(fileService.createDownloadUrl(tokenString));
  }
}
