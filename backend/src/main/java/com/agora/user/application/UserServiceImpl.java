package com.agora.user.application;

import com.agora.exception.BadRequestException;
import com.agora.media.ImageStorageService;
import com.agora.security.CustomUser;
import com.agora.user.RegisterUserRequest;
import com.agora.user.UserResponse;
import com.agora.user.UserService;
import com.agora.user.domain.User;
import com.agora.user.domain.UserRole;
import com.agora.user.persistence.UserRepository;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ImageStorageService imageStorageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterUserRequest request) {
        String avatarUrl = null;
        MultipartFile avatar = request.avatar();
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = imageStorageService.upload(request.avatar());
        }

        if (userRepository.existsByUsernameAndEmail(request.username(), request.email())) {
            if (avatarUrl != null) {
                imageStorageService.delete(avatarUrl);
            }
            throw new BadRequestException("Tên đăng nhập hoặc email đã tồn tại");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        if (avatarUrl != null) {
            user.setAvatar(avatarUrl);
        }
        try {
            return userMapper.toResponse(userRepository.save(user));
        } catch (Exception ex) {
            imageStorageService.delete(avatarUrl);
            throw ex;
        }
    }

    @Override
    public UserDetails loadUserByUsername(@NotNull String username)
            throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user"));

        Set<GrantedAuthority> authorities =
                Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new CustomUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getFullName(),
                user.getAvatar(),
                user.getRole().name(),
                authorities);
    }
}
