package com.datashare.api.controller;

import com.datashare.api.dto.FileMetadataResponse;
import com.datashare.api.dto.PresignedDownloadResponse;
import com.datashare.api.dto.PresignedUploadRequest;
import com.datashare.api.dto.PresignedUploadResponse;
import com.datashare.api.entities.User;
import com.datashare.api.repository.FileRepository;
import com.datashare.api.service.FileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

  @Autowired private final FileService fileService;

  @Autowired private final FileRepository fileRepository;

  /**
   * Get an upload presigned URL
   *
   * @param authentication the authentication from JWT filter
   * @param body the presigned upload request body
   * @return
   */
  @PostMapping("/upload")
  public ResponseEntity<PresignedUploadResponse> presignedUpload(
      Authentication authentication, @RequestBody PresignedUploadRequest body) {

    // Get authenticated user
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User user = (User) authentication.getPrincipal();

    return ResponseEntity.ok(
        fileService.createUploadUrl(
            body.getFilename(),
            body.getContentType(),
            body.getSize(),
            body.getExpirationDays(),
            user.getId()));
  }

  /**
   * Get the current user files
   *
   * @param authentication the authentication from JWT filter
   * @return
   */
  @GetMapping("/my")
  public ResponseEntity<List<FileMetadataResponse>> myFiles(Authentication authentication) {
    // Get authenticated user
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User user = (User) authentication.getPrincipal();

    return ResponseEntity.ok(
        this.fileRepository.findByUserIdWithToken(user.getId()).stream()
            .map(FileMetadataResponse::fromEntity)
            .toList());
  }

  /**
   * Get a presigned download URL for a file
   *
   * @param authentication the authentication from JWT filter
   * @param tokenString the download token string
   * @return
   */
  @GetMapping("/download/{tokenString}")
  public ResponseEntity<PresignedDownloadResponse> presignedDownload(
      @PathVariable String tokenString) throws Exception {

    // Response with file metadata and download link
    return ResponseEntity.ok(fileService.createDownloadUrl(tokenString));
  }

  @DeleteMapping("/my/{tokenString}")
  public ResponseEntity<?> deleteMyFile(
      Authentication authentication, @PathVariable String tokenString) throws Exception {

    // Get authenticated user
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User user = (User) authentication.getPrincipal();

    fileService.deleteMyFile(user, tokenString);

    return ResponseEntity.status(HttpStatus.NO_CONTENT.value()).build();
  }
}
