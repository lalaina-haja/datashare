package com.datashare.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.datashare.api.dto.PresignedUploadRequest;
import com.datashare.api.dto.PresignedUploadResponse;
import com.datashare.api.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class PublicFileControllerTest {

  @Mock private FileService fileService;

  @InjectMocks private FileController fileController;

  @Test
  void should_return_presigned_upload_for_anonymous() throws Exception {
    // Mock file service
    PresignedUploadResponse resp = new PresignedUploadResponse("http://s3", "TOK", null);

    when(fileService.createUploadUrl(eq("test.txt"), eq("text/plain"), eq(123L), any(), eq(null)))
        .thenReturn(resp);

    // WHEN upload anonymous
    ResponseEntity<PresignedUploadResponse> result =
        fileController.presignedUploadAnonymous(
            new PresignedUploadRequest("test.txt", "text/plain", 123L, null));
    PresignedUploadResponse response = result.getBody();

    // THEN the controller returns 200
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getUploadUrl()).isEqualTo("http://s3");
    assertThat(response.getTokenString()).isEqualTo("TOK");
  }
}
