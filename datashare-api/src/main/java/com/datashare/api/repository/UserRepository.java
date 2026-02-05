package com.datashare.api.repository;

import com.datashare.api.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
/**
 * JPA repository for {@link com.datashare.api.entities.User} entities.
 *
 * <p>Provides CRUD operations and convenience methods to locate users by email.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Find a user by their email address.
   *
   * @param email the user's email
   * @return an Optional containing the user when found
   */
  Optional<User> findByEmail(String email);

  /**
   * Load a user by username for Spring Security.
   *
   * <p>This delegates to {@link #findByEmail(String)} and throws a {@link
   * org.springframework.security.core.userdetails.UsernameNotFoundException} when the user cannot
   * be found.
   *
   * @param email the login email
   * @return the {@link org.springframework.security.core.userdetails.UserDetails}
   */
  default UserDetails loadUserByUsername(String email) {
    User user =
        findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    return user;
  }
}
