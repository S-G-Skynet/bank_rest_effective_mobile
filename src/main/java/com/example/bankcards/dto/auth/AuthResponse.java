package com.example.bankcards.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Ответ с токеном")
public class AuthResponse {
    @Schema(example = "eyJhbGciOiJIUzM4NCJ9...")
    private String accessToken;
}

