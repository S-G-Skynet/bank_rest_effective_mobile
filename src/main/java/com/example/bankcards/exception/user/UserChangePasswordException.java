package com.example.bankcards.exception.user;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserChangePasswordException extends ApiException {
    public UserChangePasswordException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
