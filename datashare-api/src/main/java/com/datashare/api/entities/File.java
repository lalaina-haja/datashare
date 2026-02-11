package com.datashare.api.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;

@Entity(name = "FileEntity")
@Table(name = "files")
@Data
public class File {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  Long userId;

  String filename;

  String contentType;

  Long size;

  @Column(nullable = false)
  String s3Key;

  @OneToOne(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
  Token token;

  @Column(updatable = false)
  Instant createdAt;
}
