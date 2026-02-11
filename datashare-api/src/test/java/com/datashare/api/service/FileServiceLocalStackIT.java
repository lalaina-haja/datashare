package com.datashare.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.datashare.api.configuration.S3Properties;
import com.datashare.api.dto.PresignedDownloadResponse;
import com.datashare.api.dto.PresignedUploadResponse;
import com.datashare.api.entities.File;
import com.datashare.api.entities.User;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/** FileService Integration Test with Localstack S3 */
@SpringBootTest()
@ActiveProfiles("test")
@Testcontainers
public class FileServiceLocalStackIT {

  private static final DockerImageName LOCALSTACK_IMAGE =
      DockerImageName.parse("localstack/localstack:3.0.2");

  @Container
  static LocalStackContainer localstack =
      new LocalStackContainer(LOCALSTACK_IMAGE).withServices(LocalStackContainer.Service.S3);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "aws.s3.endpoint",
        () -> localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
    registry.add("aws.s3.region", () -> "us-east-1");
    registry.add("aws.s3.access-key", localstack::getAccessKey);
    registry.add("aws.s3.secret-key", localstack::getSecretKey);
    registry.add("aws.s3.path-style-access", () -> true);
  }

  @Autowired private S3Client s3Client;

  @Autowired private S3Properties s3Properties;

  @Autowired private TokenService tokenService;

  @Autowired private FileService fileService;

  @BeforeEach
  void setup() {
    // Replace S3 endpoint with the localstack
    s3Properties.setEndpoint(
        localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());

    // Create bucket
    s3Client.createBucket(CreateBucketRequest.builder().bucket(s3Properties.getBucket()).build());
  }

  /** Test that a presigned URL is got from S3 localstack and the upload works */
  @Test
  @DisplayName("INTEG-S3-001: Generate presigned and upload file")
  public void shouldGeneratePresignedUrlAndUploadToS3() throws Exception {
    // WHEN create upload URL
    PresignedUploadResponse res =
        fileService.createUploadUrl("test.png", "image/png", 1000, 7, null);

    // THEN generated upload URL is not null
    assertNotNull(res.getUploadUrl());
    assertNotNull(res.getTokenString());
    assertNotNull(res.getExpiresAt());

    // AND the file metadata are stored
    File file = tokenService.validateToken(res.getTokenString());
    assertEquals("test.png", file.getFilename());
    assertEquals("image/png", file.getContentType());

    // AND the S3 file upload is OK
    HttpURLConnection conn =
        (HttpURLConnection) new URI(res.getUploadUrl()).toURL().openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    conn.setRequestProperty("Content-Type", "image/png");
    conn.getOutputStream().write("hello".getBytes());
    assertEquals(200, conn.getResponseCode());
    conn.disconnect();
  }

  /** Test that a presigned download URL is generated and file can be downloaded */
  @Test
  @DisplayName("INTEG-S3-002: Generate presigned download URL and download file")
  public void shouldGeneratePresignedDownloadUrlAndDownloadFromS3() throws Exception {
    // GIVEN a file is uploaded to S3
    PresignedUploadResponse uploadRes =
        fileService.createUploadUrl("document.txt", "text/plain", 100, 7, 1L);

    String fileContent = "This is test content for download";
    HttpURLConnection uploadConn =
        (HttpURLConnection) new URI(uploadRes.getUploadUrl()).toURL().openConnection();
    uploadConn.setDoOutput(true);
    uploadConn.setRequestMethod("PUT");
    uploadConn.setRequestProperty("Content-Type", "text/plain");
    uploadConn.getOutputStream().write(fileContent.getBytes());
    assertEquals(200, uploadConn.getResponseCode());
    uploadConn.disconnect();

    // WHEN create download URL
    PresignedDownloadResponse downloadRes =
        fileService.createDownloadUrl(uploadRes.getTokenString());

    // THEN generated download URL is not null
    assertNotNull(downloadRes.getDownloadUrl());
    assertEquals("document.txt", downloadRes.getFilename());
    assertEquals("text/plain", downloadRes.getContentType());

    // AND the file can be downloaded from S3
    HttpURLConnection downloadConn =
        (HttpURLConnection) new URI(downloadRes.getDownloadUrl()).toURL().openConnection();
    downloadConn.setRequestMethod("GET");
    assertEquals(200, downloadConn.getResponseCode());

    // AND the downloaded content is correct
    InputStream inputStream = downloadConn.getInputStream();
    String downloadedContent = new String(inputStream.readAllBytes());
    assertEquals(fileContent, downloadedContent);
    downloadConn.disconnect();
  }

  @Test
  @DisplayName("INTEG-S3-003: Delete file")
  void shouldDeleteFileCompleteLifecycle() throws Exception {

    // GIVEN the user
    User user = new User(1L, "test@mail.com", null, null);

    // CREATE + UPLOAD
    PresignedUploadResponse uploadRes =
        fileService.createUploadUrl("delete-test.txt", "text/plain", 100, 7, 1L);
    uploadFile(uploadRes.getUploadUrl(), "test content");

    // Récupère infos DB
    File file = tokenService.validateToken(uploadRes.getTokenString());

    // Vérifie qu'elle existe
    assertObjectExists(s3Properties.getBucket(), file.getS3Key());

    // DELETE
    fileService.deleteMyFile(user, uploadRes.getTokenString());

    // Vérifie supprimé
    assertObjectDeleted(s3Properties.getBucket(), file.getS3Key());

    // CLEANUP
    cleanupBucket();
  }

  private void uploadFile(String presignedUrl, String content) throws Exception {
    HttpURLConnection conn = (HttpURLConnection) new URI(presignedUrl).toURL().openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");
    conn.setRequestProperty("Content-Type", "text/plain");
    conn.getOutputStream().write(content.getBytes());
    assertEquals(200, conn.getResponseCode());
    conn.disconnect();
  }

  private void assertObjectExists(String bucket, String key) {
    s3Client.getObject(r -> r.bucket(bucket).key(key));
  }

  private void assertObjectDeleted(String bucket, String key) {
    assertThrows(
        NoSuchKeyException.class, () -> s3Client.getObject(r -> r.bucket(bucket).key(key)));
  }

  private void cleanupBucket() {
    s3Client
        .listObjectsV2(r -> r.bucket(s3Properties.getBucket()))
        .contents()
        .forEach(
            obj -> s3Client.deleteObject(r -> r.bucket(s3Properties.getBucket()).key(obj.key())));
  }
}
