package com.agora.user.application;

import com.agora.user.UserService;
import com.agora.user.persistence.UserRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public boolean checkUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }
}
