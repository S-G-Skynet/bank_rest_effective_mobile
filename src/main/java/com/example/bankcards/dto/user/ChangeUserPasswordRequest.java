package com.example.bankcards.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangeUserPasswordRequest {
    private String oldPassword;
    private String newPassword;
}
