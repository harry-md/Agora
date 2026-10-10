package com.agora.user.application;

import com.agora.user.UserRegisterRequest;
import com.agora.user.UserResponse;
import com.agora.user.domain.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    @Mapping(target = "avatar", ignore = true)
    User toEntity(UserRegisterRequest request);
}
