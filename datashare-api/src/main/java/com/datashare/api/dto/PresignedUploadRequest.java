package com.datashare.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadRequest {

  String filename;

  String contentType;

  long size;

  Integer expirationDays;
}
