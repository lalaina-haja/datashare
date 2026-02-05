package com.datashare.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.datashare.api.dto.RegisterRequest;
import com.datashare.api.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit Test Set for UserMapper */
public class UserMapperTest {

  private final UserMapper userMapper = UserMapper.INSTANCE;

  /** Test that the user is correctly mapped */
  @Test
  @DisplayName("UNIT-MAPPER-001: Should map DTO to User")
  void shouldMapDtoToUser() {
    RegisterRequest dto = new RegisterRequest("jane@example.com", "PASSWORD");

    User user = userMapper.toEntity(dto);

    assertNotNull(user);
    assertEquals("jane@example.com", user.getEmail());
    assertEquals("PASSWORD", user.getPassword());
  }

  /** Test that null is returned for null user */
  @Test
  @DisplayName("UNIT-MAPPER-002: Should handle Null User")
  void shouldHandleNullUser() {
    assertThat(userMapper.toEntity(null)).isNull();
    ;
  }
}
