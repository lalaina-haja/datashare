package com.datashare.api.service.security;

import com.datashare.api.entities.User;
import com.datashare.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

/**
 * UserDetailsService implementation that loads user data from the application's {@link
 * com.datashare.api.repository.UserRepository}.
 */
public class CustomUserDetailService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    /**
     * Load user details by username (email).
     *
     * @param login the username/email to load
     * @return the loaded {@link UserDetails}
     * @throws UsernameNotFoundException if the user cannot be found
     */
    User user =
        userRepository
            .findByEmail(login)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));
    return user;
  }
}
