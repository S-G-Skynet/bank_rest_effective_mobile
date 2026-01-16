package com.example.bankcards.exception.user;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundByUsernameException extends ApiException {
    public UserNotFoundByUsernameException(String username) {
        super("User not found with username: " + username, HttpStatus.NOT_FOUND);
    }
}
