package com.datashare.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.datashare.api.dto.PresignedUploadRequest;
import com.datashare.api.dto.PresignedUploadResponse;
import com.datashare.api.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = PublicFileController.class)
public class PublicFileControllerTest {

  @Autowired private MockMvc mvc;

  @MockBean private FileService fileService;

  @Test
  void should_return_presigned_upload_for_anonymous() throws Exception {
    PresignedUploadResponse resp = new PresignedUploadResponse("http://s3", "TOK", null);

    when(fileService.createUploadUrl(eq("test.txt"), eq("text/plain"), eq(123L), any(), eq(null)))
        .thenReturn(resp);

    String body = "{\"filename\":\"test.txt\",\"contentType\":\"text/plain\",\"size\":123}";

    mvc.perform(
            MockMvcRequestBuilders.post("/public/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.uploadUrl").value("http://s3"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.tokenString").value("TOK"));
  }
}
