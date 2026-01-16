package com.example.bankcards.exception.card;

import com.example.bankcards.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends ApiException {
    public InsufficientFundsException() {
        super("You don't have that much money in your balance.", HttpStatus.METHOD_NOT_ALLOWED);
    }
}
