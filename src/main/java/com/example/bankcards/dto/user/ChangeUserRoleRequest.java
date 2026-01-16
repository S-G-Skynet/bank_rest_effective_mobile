package com.example.bankcards.dto.user;

import com.example.bankcards.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangeUserRoleRequest {
    private UserRole role;
}
