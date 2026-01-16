package com.example.bankcards.service;

import com.example.bankcards.dto.user.*;
import com.example.bankcards.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {

    UserDto create(CreateUserRequest request, UserRole role);

    UserDto getById(Long id);

    Page<UserDto> getAll(Pageable pageable);

    UserDto updateUsername(Long id, ChangeUserUsernameRequest request);
    UserDto updateRole(Long id, ChangeUserRoleRequest request);
    void updatePassword(Long id, ChangeUserPasswordRequest request);

    void delete(Long id);
}
