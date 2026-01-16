package com.example.bankcards.config.mapper;

import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.User;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
