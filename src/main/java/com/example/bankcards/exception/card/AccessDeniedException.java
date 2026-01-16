package com.example.bankcards.exception.card;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApiException {
    public AccessDeniedException(Long userId) {
        super("Cards do not belong to user with id: " + userId, HttpStatus.FORBIDDEN);
    }
}
