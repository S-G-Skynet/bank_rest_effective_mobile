package com.example.bankcards.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangeUserUsernameRequest {
    private String username;
}
