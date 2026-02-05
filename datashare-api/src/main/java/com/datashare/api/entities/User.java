package com.datashare.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * Represents an application user persisted in the database.
 *
 * <p>Implements {@link org.springframework.security.core.userdetails.UserDetails} so it can be used
 * by Spring Security during authentication.
 */
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  /** Primary identifier of the user. */
  Long id;

  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email must be valid")
  @Column(nullable = false, unique = true)
  /** Unique email address used as the login identifier. */
  String email;

  @NotBlank(message = "Password is mandatory")
  @Column(nullable = false)
  /** Encoded password of the user. */
  String password;

  @CreatedDate
  @Column(updatable = false)
  /** Creation timestamp of the record (not updatable). */
  LocalDateTime createdAt;

  @Override
  /**
   * Returns the login identifier of the user (here the email).
   *
   * @return the email used as username
   */
  public String getUsername() {
    return email;
  }

  @Override
  /**
   * Authorities (roles) granted to the user.
   *
   * @return a collection of Spring Security authorities
   */
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  /** Account non-expired. */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /** Account non-locked. */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /** Credentials non-expired. */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /** Account enabled. */
  @Override
  public boolean isEnabled() {
    return true;
  }
}
