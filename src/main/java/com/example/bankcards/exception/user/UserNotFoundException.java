package com.example.bankcards.exception.user;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId, HttpStatus.NOT_FOUND);
    }
}
