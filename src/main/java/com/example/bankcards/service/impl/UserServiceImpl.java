package com.example.bankcards.service.impl;

import com.example.bankcards.config.mapper.UserMapper;
import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.user.UserChangePasswordException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.exception.user.UserWithThisUsernameAlreadyExist;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.PasswordConfig;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordConfig passwordConfig;
    private final UserMapper userMapper;

    @Override
    public UserDto create(CreateUserRequest request, UserRole role) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserWithThisUsernameAlreadyExist(request.getUsername());
        }
        String hash = passwordConfig.passwordEncoder()
                .encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(hash)
                .role(role)
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long id) {
        return userMapper.toDto(
                userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id))
        );
    }

    @Override
    public Page<UserDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public UserDto updateUsername(Long id, ChangeUserUsernameRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserWithThisUsernameAlreadyExist(request.getUsername());
        }
        user.setUsername(request.getUsername());

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto updateRole(Long id, ChangeUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setRole(request.getRole());

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void updatePassword(Long id, ChangeUserPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        PasswordEncoder passwordEncoder = passwordConfig.passwordEncoder();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new UserChangePasswordException("Invalid current password");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new UserChangePasswordException("The new password must be different from the old one");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
