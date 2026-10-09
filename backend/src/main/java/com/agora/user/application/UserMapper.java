package com.agora.user.application;

import com.agora.user.UserResponse;
import com.agora.user.domain.User;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
