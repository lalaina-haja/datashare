package com.datashare.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.datashare.api.dto.RegisterRequestDto;
import com.datashare.api.entities.User;
import org.junit.jupiter.api.Test;

public class UserMapperTest {

  private final UserMapper userMapper = UserMapper.INSTANCE;

  @Test
  void shouldMapDtoToUser() {
    RegisterRequestDto dto = new RegisterRequestDto("jane@example.com", "PASSWORD");

    User user = userMapper.toEntity(dto);

    assertNotNull(user);
    assertEquals("jane@example.com", user.getEmail());
    assertEquals("PASSWORD", user.getPassword());
  }

  @Test
  void shouldHandleNullUser() {
    assertThat(userMapper.toEntity(null)).isNull();
    ;
  }
}
