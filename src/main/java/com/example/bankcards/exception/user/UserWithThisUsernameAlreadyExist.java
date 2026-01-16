package com.example.bankcards.exception.user;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserWithThisUsernameAlreadyExist extends ApiException {
    public UserWithThisUsernameAlreadyExist(String username) {
        super("User already exist with username: " + username, HttpStatus.CONFLICT);
    }
}
