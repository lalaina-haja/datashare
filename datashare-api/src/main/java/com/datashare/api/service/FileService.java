package com.datashare.api.service;

import com.datashare.api.configuration.S3Properties;
import com.datashare.api.dto.PresignedDownloadResponse;
import com.datashare.api.dto.PresignedUploadResponse;
import com.datashare.api.entities.File;
import com.datashare.api.entities.Token;
import com.datashare.api.entities.User;
import com.datashare.api.handler.UserNotFileOwnerException;
import com.datashare.api.repository.FileRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/** File Service */
@Service
@RequiredArgsConstructor
public class FileService {

  private final S3Presigner presigner;
  private final S3Properties properties;

  @Autowired private final FileRepository fileRepository;

  @Autowired private final TokenService tokenService;

  @Autowired private final S3Client s3Client;

  private static final long MAX_SIZE = 1_000_000_000L;
  private static final Set<String> FORBIDDEN_EXT = Set.of("exe", "bat", "sh");

  /**
   * Create a presigned upload URL (validity 10 minutes)
   *
   * @param filename the file name to upload
   * @param contentType the file content type
   * @param size the file size
   * @param expirationDays the token sharing expiration days
   * @param userId the authenticated user
   * @return a PresignedUploadResponse object
   */
  public PresignedUploadResponse createUploadUrl(
      String filename, String contentType, long size, Integer expirationDays, Long userId) {
    // Check max file size
    if (size > MAX_SIZE) {
      throw new IllegalArgumentException("File too large (max 1 Go)");
    }

    // Check forbidden extension
    String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    if (FORBIDDEN_EXT.contains(ext)) {
      throw new IllegalArgumentException("File type not allowed");
    }

    // Generate upload URL
    String key = "uploads/" + UUID.randomUUID() + "-" + filename;

    PutObjectRequest putReq =
        PutObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .contentType(contentType)
            .build();

    PutObjectPresignRequest presignReq =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .putObjectRequest(putReq)
            .build();

    PresignedPutObjectRequest presigned = presigner.presignPutObject(presignReq);

    // Calculate expiresAt value
    Instant now = Instant.now();
    Instant expiresAt = now.plus(Duration.ofDays(expirationDays != null ? expirationDays : 7));

    // Create file metadata
    File entity = new File();
    entity.setUserId(userId);
    entity.setFilename(filename);
    entity.setContentType(contentType);
    entity.setSize(size);
    entity.setS3Key(key);
    entity.setCreatedAt(now);
    this.fileRepository.save(entity);

    // Generate download token
    Token token = this.tokenService.generateToken(entity, expiresAt);

    return new PresignedUploadResponse(
        presigned.url().toString(), token.getTokenString(), expiresAt);
  }

  /**
   * Create a presigned download URL (validity 10 minutes)
   *
   * @param tokenString the file token
   * @return a PresignedDownloadResponse object
   */
  public PresignedDownloadResponse createDownloadUrl(String tokenString) throws Exception {

    // Get file metadata
    File file = tokenService.validateToken(tokenString);

    // Generate presigned download URL
    GetObjectRequest getReq =
        GetObjectRequest.builder().bucket(properties.getBucket()).key(file.getS3Key()).build();

    GetObjectPresignRequest presignReq =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(getReq)
            .build();

    PresignedGetObjectRequest presigned = presigner.presignGetObject(presignReq);

    return new PresignedDownloadResponse(
        file.getFilename(),
        file.getContentType(),
        file.getSize(),
        presigned.url().toString(),
        file.getCreatedAt(),
        file.getToken().getExpiresAt());
  }

  /**
   * Delete the user's file
   *
   * @param user the owner
   * @param tokenString the file token string
   * @throws Exception if invalid token or user not owner of the file
   */
  public void deleteMyFile(User user, String tokenString) throws Exception {

    File file = this.tokenService.validateToken(tokenString);

    if (!file.getUserId().equals(user.getId())) {
      throw new UserNotFileOwnerException("User is not owner of the file");
    }

    this.fileRepository.delete(file);

    DeleteObjectRequest deleteRequest =
        DeleteObjectRequest.builder().bucket(properties.getBucket()).key(file.getS3Key()).build();

    s3Client.deleteObject(deleteRequest);
  }
}
