package com.agora.user.application;

import com.agora.user.RegisterUserRequest;
import com.agora.user.UserResponse;
import com.agora.user.domain.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    User toEntity(RegisterUserRequest request);
}
