package com.datashare.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "tokens",
    uniqueConstraints = @UniqueConstraint(name = "uk_token", columnNames = "tokenString"))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(unique = true, nullable = false, length = 12)
  String tokenString;

  @OneToOne
  @JoinColumn(name = "file_id", nullable = false)
  File file;

  @Column(nullable = false)
  Instant expiresAt;
}
