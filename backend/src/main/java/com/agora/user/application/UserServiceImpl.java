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
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ImageStorageService imageStorageService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Override
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByUsernameOrEmail(request.username(), request.email())) {
            throw new BadRequestException("Tên đăng nhập hoặc email đã tồn tại");
        }

        MultipartFile avatar = request.avatar();
        final String avatarUrl =
                avatar != null && !avatar.isEmpty() ? imageStorageService.upload(avatar) : null;
        final String hashedPassword = passwordEncoder.encode(request.password());

        try {
            return transactionTemplate.execute(status -> {
                if (userRepository.existsByUsernameOrEmail(request.username(), request.email())) {
                    throw new BadRequestException("Tên đăng nhập hoặc email đã tồn tại");
                }

                User user = userMapper.toEntity(request);
                user.setPassword(hashedPassword);
                user.setRole(UserRole.CUSTOMER);
                if (avatarUrl != null) {
                    user.setAvatar(avatarUrl);
                }
                return userMapper.toResponse(userRepository.save(user));
            });
        } catch (RuntimeException ex) {
            if (avatarUrl != null) {
                try {
                    imageStorageService.delete(avatarUrl);
                } catch (RuntimeException | IOException e) {
                    ex.addSuppressed(e);
                    log.error("Error while trying cleanup user's avatar", e);
                }
            }
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
