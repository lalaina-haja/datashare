package com.datashare.api.mapper;

import com.datashare.api.dto.RegisterRequest;
import com.datashare.api.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper interface for converting between User entities and DTOs.
 *
 * <p>Uses MapStruct to automatically generate the implementation for mapping RegisterRequestDto to
 * User entity, ignoring certain fields during the mapping process.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "authorities", ignore = true)
  User toEntity(RegisterRequest dto);
}
