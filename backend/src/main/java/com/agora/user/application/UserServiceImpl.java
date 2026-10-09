package com.agora.user.application;

import com.agora.address.AddressRequest;
import com.agora.address.AddressResponse;
import com.agora.address.AddressService;
import com.agora.exception.Conflict;
import com.agora.exception.ResourceNotFound;
import com.agora.user.RegisterRequest;
import com.agora.user.UserResponse;
import com.agora.user.UserService;
import com.agora.user.domain.User;
import com.agora.user.persistence.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressService addressService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean checkUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim();
        String username = request.username().trim();
        String fullName = request.fullName().trim();

        if (userRepository.existsByUsername(username)) {
            throw new Conflict("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new Conflict("Email đã tồn tại");
        }

        UUID addressId = addressService.addAddress(request.address());
        User user = User.builder()
                .email(email)
                .username(username)
                .fullName(fullName)
                .password(passwordEncoder.encode(request.password()))
                .addressId(addressId)
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddress(UUID userId) {
        return addressService.getAddress(findUser(userId).getAddressId());
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID userId, AddressRequest request) {
        return addressService.updateAddress(findUser(userId).getAddressId(), request);
    }

    private User findUser(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFound("Không tìm thấy người dùng"));
    }
}
