package com.example.bankcards.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Json запрос для login")
public class LoginRequest {
    @Schema(example = "admin")
    private String username;
    @Schema(example = "password")
    private String password;
}

